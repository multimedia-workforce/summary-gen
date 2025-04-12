export type Transcription = {
    id: string;
    text: string;
    time: number;
};

export type Summary = {
    id: string;
    text: string;
    time: number;
};

export type SmartSession = {
    id: string;
    transcription: Transcription;
    summary: Summary;
};

export async function fetchSmartSessions(): Promise<SmartSession[]> {
    return new Promise((resolve, reject) => {
       return fetch("api/persistence")
           .then(res => res.json())
           .then(data => resolve(data as SmartSession[]))
           .catch(reject);
    });
}