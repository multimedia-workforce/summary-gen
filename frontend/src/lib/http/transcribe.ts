import type { TranscribeResponse } from "../../routes/api/transcribe/+server";

/**
 * Sends a file to the transcribe API and yields streamed responses.
 *
 * @param file The audio file to transcribe.
 * @param signal Optional AbortSignal to cancel the request.
 */
export async function* streamTranscription(
    file: File,
    signal?: AbortSignal
): AsyncGenerator<TranscribeResponse> {
    const formData = new FormData();
    formData.append("file", file);

    const response = await fetch("/api/transcribe", {
        method: "POST",
        body: formData,
        signal,
    });

    if (!response.body) return;

    const reader = response.body.getReader();
    const decoder = new TextDecoder();

    try {
        while (true) {
            const { done, value } = await reader.read();
            if (done) break;

            const decoded = decoder.decode(value).trim();
            if (!decoded) continue;

            for (const chunk of decoded.split("\n")) {
                try {
                    const data: TranscribeResponse = JSON.parse(atob(chunk));
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
            // Request was intentionally aborted – silent fail or optional log
        } else if (err instanceof Error) {
            console.error("Stream failed:", err.message);
        } else {
            console.error("Unknown error during transcription stream:", err);
        }
    } finally {
        reader.releaseLock();
    }
}

/**
 * Checks the transcribe API for a heartbeat
 * @returns Whether there is a heartbeat
 */
export async function hasHeartbeat(): Promise<boolean> {
    return new Promise((resolve) => {
        fetch("/api/transcribe/heartbeat")
            .then((response) => response.json())
            .then((data) => resolve(data.status === "OK"))
            .catch(() => resolve(false));
    });
}
