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