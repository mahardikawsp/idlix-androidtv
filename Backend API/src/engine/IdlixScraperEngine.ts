import puppeteer from 'puppeteer-extra';
import StealthPlugin from 'puppeteer-extra-plugin-stealth';
import { Browser, Page } from 'puppeteer';

// Add stealth plugin to evade bot/Cloudflare detection
const puppeteerInstance = (puppeteer as any).default || puppeteer;
puppeteerInstance.use(StealthPlugin());

export interface ResolvedPlaybackResult {
  title: string;
  hlsMasterUrl: string;
  subtitles: Array<{ lang: string; label: string; path: string }>;
  durationSec: number;
  videoId: string;
}

export class IdlixScraperEngine {
  private static instance: IdlixScraperEngine | null = null;
  private browser: Browser | null = null;
  private page: Page | null = null;
  private isReady = false;
  private initPromise: Promise<void> | null = null;
  private baseUrl = process.env.IDLIX_BASE_URL || 'https://z2.idlixku.com';

  private constructor() {}

  public static getInstance(): IdlixScraperEngine {
    if (!IdlixScraperEngine.instance) {
      IdlixScraperEngine.instance = new IdlixScraperEngine();
    }
    return IdlixScraperEngine.instance;
  }

  public async init(): Promise<void> {
    if (this.isReady && this.page && !this.page.isClosed()) {
      return;
    }
    if (this.initPromise) {
      return this.initPromise;
    }

    this.initPromise = (async () => {
      console.log(`[ScraperEngine] Launching stealth browser to bypass Cloudflare at ${this.baseUrl}...`);
      try {
        const executablePath = process.env.PUPPETEER_EXECUTABLE_PATH || undefined;
        const proxyArg = process.env.PROXY_SERVER ? [`--proxy-server=${process.env.PROXY_SERVER}`] : [];

        this.browser = await (puppeteerInstance as any).launch({
          headless: true,
          executablePath,
          args: [
            '--no-sandbox',
            '--disable-setuid-sandbox',
            '--disable-dev-shm-usage',
            '--disable-gpu',
            '--no-first-run',
            '--no-zygote',
            '--disable-extensions',
            '--disable-blink-features=AutomationControlled',
            '--window-size=1920,1080',
            '--lang=id-ID,id,en-US,en',
            ...proxyArg,
          ],
        });

        const pages = await this.browser!.pages();
        this.page = pages.length > 0 ? pages[0] : await this.browser!.newPage();

        await this.page.setViewport({ width: 1920, height: 1080 });

        // Advanced stealth: mask navigator.webdriver and chrome runtime signatures
        await this.page.evaluateOnNewDocument(() => {
          Object.defineProperty(navigator, 'webdriver', {
            get: () => undefined,
          });
          (window as any).chrome = {
            runtime: {},
            loadTimes: () => {},
            csi: () => {},
            app: {},
          };
        });

        await this.page.setUserAgent(
          'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36'
        );

        await this.page.setExtraHTTPHeaders({
          'Accept-Language': 'id-ID,id;q=0.9,en-US;q=0.8,en;q=0.7',
          'Sec-Ch-Ua': '"Not(A:Brand";v="99", "Google Chrome";v="133", "Chromium";v="133"',
          'Sec-Ch-Ua-Mobile': '?0',
          'Sec-Ch-Ua-Platform': '"Windows"',
        });

        // Recommendation 1: Never block fonts, images, or Cloudflare challenge assets during initialization
        // Only block heavy third-party tracking/ad domains to prevent interference with Turnstile profiling
        await this.page.setRequestInterception(true);
        this.page.on('request', (req) => {
          const url = req.url();
          const adBlock = ['xyzchat', 'doubleclick', 'google-analytics', 'analytics.google.com'];

          if (adBlock.some((d) => url.includes(d))) {
            req.abort();
          } else {
            req.continue();
          }
        });

        console.log(`[ScraperEngine] Navigating to ${this.baseUrl} to solve challenge...`);
        await this.page.goto(this.baseUrl, {
          waitUntil: 'domcontentloaded',
          timeout: 45000,
        });

        // Recommendation 2: Cloudflare Turnstile detection & auto-solving
        const isChallengeActive = async () => {
          try {
            const title = (await this.page!.title()).toLowerCase();
            return title.includes('just a moment') || title.includes('attention required');
          } catch (_) {
            return false;
          }
        };

        if (await isChallengeActive()) {
          console.log('[ScraperEngine] Cloudflare challenge detected, monitoring Turnstile resolution...');

          // Give Turnstile up to 30s to solve automatically or attempt interaction
          for (let attempt = 1; attempt <= 15; attempt++) {
            await new Promise((r) => setTimeout(r, 2000));

            if (!(await isChallengeActive())) {
              break;
            }

            try {
              const frames = this.page!.frames();
              for (const frame of frames) {
                if (frame.url().includes('challenges.cloudflare.com')) {
                  const checkbox = await frame.$(
                    'input[type="checkbox"], .ctp-checkbox-label, #challenge-stage, .mark'
                  );
                  if (checkbox) {
                    console.log(`[ScraperEngine] Attempting interaction with Turnstile widget (attempt ${attempt})...`);
                    await checkbox.click().catch(() => {});
                  }
                }
              }
            } catch (_) {}
          }
        }

        // Wait until Cloudflare challenge passes
        await this.page.waitForFunction(
          () => {
            const title = document.title.toLowerCase();
            return !title.includes('just a moment') && !title.includes('attention required');
          },
          { timeout: 35000 }
        );

        console.log(`[ScraperEngine] Cloudflare challenge bypassed successfully! Page Title: "${await this.page.title()}"`);
        this.isReady = true;
      } catch (err: any) {
        console.error(`[ScraperEngine] Failed to initialize browser session: ${err?.message || err}`);
        this.isReady = false;
        throw err;
      } finally {
        this.initPromise = null;
      }
    })();

    return this.initPromise;
  }

  public getSessionStatus(): { isReady: boolean; targetUrl: string } {
    return {
      isReady: this.isReady && this.page !== null && !this.page.isClosed(),
      targetUrl: this.baseUrl,
    };
  }

  public async executeFetch<T = any>(
    endpoint: string,
    options: {
      method?: 'GET' | 'POST';
      body?: any;
      headers?: Record<string, string>;
      timeoutMs?: number;
    } = {}
  ): Promise<{ status: number; data: T }> {
    await this.init();

    if (!this.page || this.page.isClosed()) {
      throw new Error('Browser page session is closed');
    }

    const { method = 'GET', body, headers = {} } = options;
    const url = endpoint.startsWith('http') ? endpoint : `${this.baseUrl}${endpoint.startsWith('/') ? '' : '/'}${endpoint}`;

    try {
      const result = await this.page.evaluate(
        async (fetchUrl, fetchMethod, fetchBody, fetchHeaders) => {
          try {
            const res = await fetch(fetchUrl, {
              method: fetchMethod,
              headers: {
                Accept: 'application/json, text/plain, */*',
                'Content-Type': 'application/json',
                ...fetchHeaders,
              },
              body: fetchBody ? JSON.stringify(fetchBody) : undefined,
            });

            const text = await res.text();
            let parsedData = null;
            try {
              parsedData = JSON.parse(text);
            } catch (_) {
              parsedData = text;
            }

            return {
              status: res.status,
              data: parsedData,
              error: null,
            };
          } catch (e: any) {
            return {
              status: 500,
              data: null,
              error: e?.message || 'In-browser fetch error',
            };
          }
        },
        url,
        method,
        body,
        headers
      );

      if (result.error) {
        throw new Error(result.error);
      }

      // If Cloudflare returns 403 interstitial, mark session invalid so it re-solves next time
      if (
        result.status === 403 &&
        typeof result.data === 'string' &&
        (result.data.includes('Just a moment') ||
          result.data.includes('cf-mitigated') ||
          result.data.includes('challenges.cloudflare.com'))
      ) {
        console.warn(`[ScraperEngine] Received Cloudflare 403 challenge for ${url}. Invalidating scraper session.`);
        this.isReady = false;
        this.page = null;
      }

      return { status: result.status, data: result.data };
    } catch (err: any) {
      console.warn(`[ScraperEngine] Request failed for ${url}: ${err?.message}`);
      // If session might be stale or blocked, trigger re-init in background
      if (err?.message?.includes('Target closed') || err?.message?.includes('Session closed')) {
        this.isReady = false;
        this.page = null;
      }
      throw err;
    }
  }

  /**
   * Resolves complete HLS playback in a single call for high performance
   */
  public async resolvePlayback(
    contentType: string,
    contentId: string,
    contentTitle?: string
  ): Promise<ResolvedPlaybackResult> {
    await this.init();

    let targetType = contentType;
    let targetId = contentId;

    // Handle series: if tv_series is requested, resolve first episode of season 1
    if (contentType === 'tv_series') {
      const seriesRes = await this.executeFetch(`/api/series/${contentId}`);
      const series = seriesRes.data;
      const firstEp =
        series?.defaultSeason?.episodes?.[0] ||
        (series?.seasons?.[0]
          ? (await this.executeFetch(`/api/series/${series.slug}/season/${series.seasons[0].seasonNumber}/episode/1`)).data?.episode
          : null);

      if (firstEp?.id) {
        targetType = 'episode';
        targetId = firstEp.id;
      } else {
        throw new Error('No episodes available for this series');
      }
    }

    // Step 1: Get play-info (gateToken + countdown)
    const playInfoRes = await this.executeFetch(`/api/watch/play-info/${targetType}/${targetId}`);
    const playInfo = playInfoRes.data;
    if (!playInfo?.gateToken) {
      throw new Error('Failed to obtain playback gate token from provider');
    }

    // Extract device ID (did) from gateToken payload
    let didCookie: string | undefined;
    try {
      const rawP0 = playInfo.gateToken.split('.')[0];
      const decodedP0 = Buffer.from(rawP0, 'base64').toString('utf-8');
      const didMatch = /"owner":"d:([^"]+)"/.exec(decodedP0);
      if (didMatch?.[1]) {
        didCookie = `did=${didMatch[1]}`;
      }
    } catch (_) {}

    // Step 2: Handle unlock countdown
    const serverNow = playInfo.serverNow || Date.now();
    const unlockAt = playInfo.unlockAt || serverNow + 15000;
    const waitMs = Math.max(0, unlockAt - serverNow);

    if (waitMs > 0) {
      console.log(`[ScraperEngine] Synchronizing with provider countdown (${Math.round(waitMs / 1000)}s)...`);
      await new Promise((resolve) => setTimeout(resolve, Math.min(waitMs + 500, 16000)));
    }

    // Step 3: Claim session
    let claimRes: any = null;
    for (let attempt = 1; attempt <= 10; attempt++) {
      try {
        const claimResp = await this.executeFetch('/api/watch/session/claim', {
          method: 'POST',
          body: { gateToken: playInfo.gateToken },
          headers: didCookie ? { Cookie: didCookie } : undefined,
        });

        if (claimResp.data?.kind === 'pentos' && claimResp.data?.claim) {
          claimRes = claimResp.data;
          break;
        }
      } catch (_) {}
      await new Promise((r) => setTimeout(r, 600));
    }

    if (!claimRes?.claim) {
      throw new Error('Failed to claim playback session from provider');
    }

    // Step 4: Redeem HLS master playlist from Majorplay CDN
    const redeemUrl = claimRes.redeemUrl || 'https://e2e.majorplay.net/api/play';
    const redeemResp = await this.executeFetch(redeemUrl, {
      method: 'POST',
      body: { claim: claimRes.claim, mode: 'browser' },
      headers: {
        Referer: `${this.baseUrl}/`,
        Origin: this.baseUrl,
      },
    });

    const redeemData = redeemResp.data;
    if (!redeemData?.url) {
      throw new Error('Failed to redeem HLS master playlist URL');
    }

    return {
      title: claimRes.title || contentTitle || 'Video',
      hlsMasterUrl: redeemData.url,
      subtitles: Array.isArray(redeemData.subtitles) ? redeemData.subtitles : [],
      durationSec: claimRes.durationSec || 0,
      videoId: claimRes.videoId || '',
    };
  }

  public async close(): Promise<void> {
    if (this.browser) {
      await this.browser.close();
      this.browser = null;
      this.page = null;
      this.isReady = false;
    }
  }
}
