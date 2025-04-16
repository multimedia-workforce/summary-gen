import { transcribe } from '$lib/grpc/transcriber';
import type { RequestHandler } from './$types';

const ETranscribeStatus = {
    PROCESSING: 'processing',
    CHUNK: 'chunk',
    COMPLETED: 'completed',
    ERROR: 'error'
} as const;

export type TranscribeStatus = typeof ETranscribeStatus[keyof typeof ETranscribeStatus];
export type TranscribeResponse = {
    status: TranscribeStatus;
    result?: string;
    id?: string;
};

function encode(obj: TranscribeResponse) {
    return btoa(JSON.stringify(obj)) + '\n';
}

export const POST: RequestHandler = async ({ request, locals }) => {
    if (!locals.user) {
        return new Response('Unauthorized', { status: 401 });
    }

    const userId = locals.user.id;
    const formData = await request.formData();
    const file = formData.get('file');

    if (!file || !(file instanceof Blob)) {
        return new Response('No file uploaded', { status: 400 });
    }

    const reader = file.stream().getReader();
    const stream = new ReadableStream({
        async start(controller) {
            let isClosed = false;

            const safeEnqueue = (obj: TranscribeResponse) => {
                if (isClosed) return;
                try {
                    controller.enqueue(encode(obj));
                } catch (err) {
                    console.error('Failed to enqueue:', err);
                    isClosed = true;
                }
            };

            let unsubscribe: (() => void) | undefined;

            try {
                safeEnqueue({ status: ETranscribeStatus.PROCESSING });

                unsubscribe = await transcribe(userId, reader, (id: string, text: string) => {
                    safeEnqueue({ status: ETranscribeStatus.CHUNK, result: text, id });
                });

                safeEnqueue({ status: ETranscribeStatus.COMPLETED });
            } catch (error) {
                safeEnqueue({
                    status: ETranscribeStatus.ERROR,
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