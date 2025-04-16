import { summarize, type Prompt } from '$lib/grpc/summarizer';
import type { RequestHandler } from './$types';

const ESummarizeStatus = {
    PROCESSING: 'processing',
    CHUNK: 'chunk',
    COMPLETED: 'completed',
    ERROR: 'error'
} as const;

export type SummarizeStatus = typeof ESummarizeStatus[keyof typeof ESummarizeStatus];
export type SummarizeResponse = {
    status: SummarizeStatus;
    result?: string;
};

function encode(obj: SummarizeResponse) {
    return btoa(JSON.stringify(obj)) + '\n';
}

export const POST: RequestHandler = async ({ request, locals }) => {
    if (!locals.user) {
        return new Response('Unauthorized', { status: 401 });
    }

    const clientRequest = (await request.json()) as Prompt;

    const summarizeRequest: Prompt = {
        ...clientRequest,
        userId: locals.user.id
    };

    const stream = new ReadableStream({
        async start(controller) {
            let isClosed = false;

            const safeEnqueue = (obj: SummarizeResponse) => {
                if (isClosed) return;
                try {
                    controller.enqueue(encode(obj));
                } catch (err) {
                    console.warn('Failed to enqueue (probably already closed):', err);
                    isClosed = true;
                }
            };

            let unsubscribe: (() => void) | undefined;

            try {
                safeEnqueue({ status: ESummarizeStatus.PROCESSING });

                unsubscribe = await summarize(summarizeRequest, (text: string) => {
                    safeEnqueue({ status: ESummarizeStatus.CHUNK, result: text });
                });

                safeEnqueue({ status: ESummarizeStatus.COMPLETED });
            } catch (error) {
                safeEnqueue({
                    status: ESummarizeStatus.ERROR,
                    result: error instanceof Error ? error.message : String(error)
                });
            } finally {
                if (unsubscribe) unsubscribe();
                isClosed = true;
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