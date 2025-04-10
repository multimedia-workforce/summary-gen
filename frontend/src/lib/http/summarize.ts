import type {SummarizeResponse} from "../../routes/api/summarize/+server";
import type {Prompt} from "@/grpc/gen/summarizer";

// Bake in user ID right now
const userId = "1cc8ed0d-191a-4aeb-8579-3d04e4c8d6b8";

/**
 * Sends a transcript to the summarize API and yields streamed responses.
 *
 * @param transcript The text to summarize.
 * @param model The model to use.
 * @param temperature The temperature to use
 * @param signal Optional AbortSignal to cancel the request.
 */
export async function* streamSummarization(
    transcript: string,
    model: string,
    temperature: number,
    signal?: AbortSignal
): AsyncGenerator<SummarizeResponse> {
    const response = await fetch("/api/summarize", {
        method: "POST",
        body: JSON.stringify({
            userId,
            transcript,
            prompt: "Summarize the following transcript",
            model,
            temperature
        } as Prompt),
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
                    const data: SummarizeResponse = JSON.parse(atob(chunk));
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

/**
 * Checks the summarize API for a heartbeat
 * @returns Whether there is a heartbeat
 */
export async function hasHeartbeat(): Promise<boolean> {
    return new Promise((resolve) => {
        fetch("/api/summarize/heartbeat")
            .then((response) => response.json())
            .then((data) => resolve(data.status === "OK"))
            .catch(() => resolve(false));
    });
}

/**
 * Retrieves a list of available models
 * @returns The models
 */
export async function getModels(): Promise<Array<string>> {
    return new Promise((resolve) => {
        fetch("/api/summarize/models")
            .then((response) => response.json())
            .then((data) => resolve(data.models))
            .catch(() => resolve([]));
    });
}