import {smartSessionPrompt, type SmartSessionPromptRequest} from "@/grpc/analytics";
import type {RequestHandler} from "./$types";

const EPromptStatus = {
    PROCESSING: "processing",
    CHUNK: "chunk",
    COMPLETED: "completed",
    ERROR: "error",
} as const;

export type PromptStatus = (typeof EPromptStatus)[keyof typeof EPromptStatus];
export type PromptResponse = {
    status: PromptStatus;
    result?: string;
};

function encode(obj: PromptResponse) {
    return btoa(JSON.stringify(obj)) + '\n';
}

export const POST: RequestHandler = async ({request, locals}) => {
    if (!locals.user) {
        return new Response('Unauthorized', {status: 401});
    }

    const clientRequest = (await request.json()) as SmartSessionPromptRequest;
    const promptRequest: SmartSessionPromptRequest = {
        ...clientRequest,
        userId: locals.user.id
    };

    const stream = new ReadableStream({
        async start(controller) {
            let isClosed = false;

            const safeEnqueue = (obj: PromptResponse) => {
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
                safeEnqueue({status: EPromptStatus.PROCESSING});

                unsubscribe = await smartSessionPrompt(promptRequest, (text: string) => {
                    safeEnqueue({status: EPromptStatus.CHUNK, result: text});
                });

                safeEnqueue({status: EPromptStatus.COMPLETED});
            } catch (error) {
                safeEnqueue({
                    status: EPromptStatus.ERROR,
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
