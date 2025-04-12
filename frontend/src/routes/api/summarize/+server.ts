import {summarize, type Prompt} from '$lib/grpc/summarizer';
import type {RequestHandler} from './$types';

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

export const POST: RequestHandler = async ({request, locals}) => {
    if (!locals.user) {
        return new Response('Unauthorized', {status: 401});
    }

    const clientRequest = (await request.json()) as Prompt;

    // Inject userId from the session into the prompt
    const summarizeRequest: Prompt = {
        ...clientRequest,
        userId: locals.user.id
    };

    const stream = new ReadableStream({
        async start(controller) {
            try {
                controller.enqueue(encode({status: ESummarizeStatus.PROCESSING}));

                await summarize(summarizeRequest, (text: string) => {
                    controller.enqueue(encode({status: ESummarizeStatus.CHUNK, result: text}));
                });

                controller.enqueue(encode({status: ESummarizeStatus.COMPLETED}));
                controller.close();
            } catch (error) {
                controller.enqueue(
                    encode({
                        status: ESummarizeStatus.ERROR,
                        result: error instanceof Error ? error.message : String(error)
                    })
                );
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
