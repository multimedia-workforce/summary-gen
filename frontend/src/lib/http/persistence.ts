export type Transcription = {
    id: string;
    text: string;
    createdAt: number;
};

export type Summary = {
    id: string;
    text: string;
    createdAt: number;
};

export type SmartSession = {
    id: string;
    transcription: Transcription;
    summary: Summary;
    createdAt: number;
};

export async function fetchSmartSessions(): Promise<SmartSession[]> {
    return new Promise((resolve, reject) => {
        return fetch("/api/persistence")
            .then(res => res.json())
            .then(data => resolve(data as SmartSession[]))
            .catch(reject);
    });
}

export async function fetchSmartSession(id: string): Promise<SmartSession> {
    return new Promise((resolve, reject) => {
        return fetch(`/api/persistence/${id}`)
            .then(res => res.json())
            .then(data => resolve(data as SmartSession))
            .catch(reject);
    });
}

/**
 * Calls the server-side API handler for deleting smart sessions
 * @param id The ID of the smart session
 * @return A promise that indicates success
 */
export async function deleteSmartSession(id: string): Promise<void> {
    return new Promise((resolve, reject) => {
        return fetch(`/api/persistence/${id}`, {
                method: 'DELETE'
            })
            .then(() => resolve())
            .catch(reject);
    });
}
