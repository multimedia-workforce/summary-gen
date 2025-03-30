import { summarize, type Prompt } from '$lib/grpc/summarizer';

const ESummarizeStatus = {
    PROCESSING: "processing",
    CHUNK: "chunk",
    COMPLETED: "completed",
    ERROR: "error"
} as const;

export type SummarizeStatus = typeof ESummarizeStatus[keyof typeof ESummarizeStatus];
export type SummarizeResponse = {
    status: SummarizeStatus;
    result?: string;
}

function encode(obj: SummarizeResponse) {
    return btoa(JSON.stringify(obj)) + '\n';
}

export async function POST({ request }) {
    const summarizeRequest = await request.json() as Prompt;
    const stream = new ReadableStream({
        async start(controller) {
            try {
                controller.enqueue(encode({ status: ESummarizeStatus.PROCESSING }));
                await summarize(summarizeRequest, (text: string) => {
                    controller.enqueue(encode({ status: ESummarizeStatus.CHUNK, result: text }));
                });
                controller.enqueue(encode({ status: ESummarizeStatus.COMPLETED }));
                controller.close();
            } catch (error) {
                controller.enqueue(encode({
                    status: ESummarizeStatus.ERROR,
                    result: error instanceof Error ? error.message : String(error)
                }));
                controller.close();
            }
        }
    });

    return new Response(stream, {
        headers: {
            'Content-Type': 'application/json',
            'Transfer-Encoding': 'chunked',
            'Connection': 'keep-alive'
        }
    });
};
