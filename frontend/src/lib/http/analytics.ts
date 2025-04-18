import type {PromptResponse} from "../../routes/api/analytics/prompt/+server";
import type {SmartSessionPromptRequest} from "@/grpc/gen/analytics";

export type HeatmapCell = {
    date: string;
    count: number;
};

export type ActivityStats = {
    firstCreatedAt: number;
    lastCreatedAt: number;
    mostActiveWeekday: string;
    avgPerDay: number;
};

export type CreateTimeStats = {
    average: number;
    median: number;
    max: number;
    min: number;
};

export type TextStats = {
    averageLength: number;
    maxLength: number;
    minLength: number;
};

export type TranscriptionMetrics = {
    totalTranscriptions: number;
    textStats: TextStats;
    createTimeStats: CreateTimeStats;
    activityStats: ActivityStats;
};

export type SummaryMetrics = {
    totalSummaries: number;
    textStats: TextStats;
    createTimeStats: CreateTimeStats;
    activityStats: ActivityStats;
};

export type SmartSessionMetrics = {
    dailyHeatmap?: Array<HeatmapCell>;
    transcriptionMetrics?: TranscriptionMetrics;
    summaryMetrics?: SummaryMetrics;
};

export type HuffmanCode = Record<string, string>;

/**
 * Sends a transcript to the prompt API
 *
 * @param model The model to use.
 * @param prompt The prompt to give
 * @param temperature The temperature to use
 * @param smartSessionIds The IDs to summarize
 * @param signal Optional AbortSignal to cancel the request.
 */
export async function* streamPrompt(
    model: string,
    prompt: string,
    temperature: number,
    smartSessionIds: string[],
    signal?: AbortSignal
): AsyncGenerator<PromptResponse> {
    const response = await fetch("/api/analytics/prompt", {
        method: "POST",
        body: JSON.stringify({
            model: model,
            prompt: prompt,
            temperature: temperature,
            smartSessionIds: smartSessionIds,
        } as SmartSessionPromptRequest),
        signal,
        headers: {
            "Content-Type": "application/json",
        },
    });

    if (!response.ok) {
        throw new Error(`HTTP Error ${response.status}: ${response.statusText}`);
    }

    if (!response.body) return;

    const reader = response.body.getReader();
    const decoder = new TextDecoder();

    try {
        while (true) {
            const {done, value} = await reader.read();
            if (done) break;

            const decoded = decoder.decode(value).trim();
            if (!decoded) continue;
            
            for (const chunk of decoded.split("\n")) {
                try {
                    const data: PromptResponse = JSON.parse(atob(chunk));
                    yield data;

                    if (data.status === "completed" || data.status === "error") {
                        return;
                    }
                } catch (err) {
                    console.error("Failed to parse chunk:", err);
                }
            }
        }
    } catch (err) {
        if (err instanceof DOMException && err.name === "AbortError") {
            // Request was aborted
        } else if (err instanceof Error) {
            console.error("Stream failed:", err.message);
        } else {
            console.error("Unknown error during summarization stream:", err);
        }
    } finally {
        reader.releaseLock();
    }
}


export async function fetchAllMetrics(): Promise<SmartSessionMetrics> {
    return new Promise((resolve, reject) => {
        return fetch("/api/analytics/metrics")
            .then(res => res.json())
            .then(data => resolve(data as SmartSessionMetrics))
            .catch(reject);
    });
}

export async function fetchSelectedMetrics(ids: Array<string>): Promise<SmartSessionMetrics> {
    return new Promise((resolve, reject) => {
        return fetch(`/api/analytics/metrics`, {
            method: 'POST',
            body: JSON.stringify({
                ids: ids
            })
        })
            .then(res => res.json())
            .then(data => resolve(data as SmartSessionMetrics))
            .catch(reject);
    });
}

export async function fetchHuffmanCode(): Promise<HuffmanCode> {
    return new Promise((resolve, reject) => {
        return fetch(`/api/analytics/huffman`)
            .then(res => res.json())
            .then(data => resolve(data as HuffmanCode))
            .catch(reject);
    });
}

export async function fetchSelectedHuffmanCode(ids: Array<string>): Promise<HuffmanCode> {
    return new Promise((resolve, reject) => {
        return fetch(`/api/analytics/huffman`, {
            method: 'POST',
            body: JSON.stringify({
                ids: ids
            })
        })
            .then(res => res.json())
            .then(data => resolve(data as HuffmanCode))
            .catch(reject);
    })
}