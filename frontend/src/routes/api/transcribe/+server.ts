import {transcribe} from '$lib/grpc/transcriber';

const ETranscribeStatus = {
    PROCESSING: "processing",
    CHUNK: "chunk",
    COMPLETED: "completed",
    ERROR: "error"
} as const;

export type TranscribeStatus = typeof ETranscribeStatus[keyof typeof ETranscribeStatus];
export type TranscribeResponse = {
    status: TranscribeStatus;
    result?: string;
}

function encode(obj: TranscribeResponse) {
    return btoa(JSON.stringify(obj)) + '\n';
}

export async function POST({request}) {
    const formData = await request.formData();
    const file = formData.get('file');

    if (!file || !(file instanceof Blob)) {
        return new Response('No file uploaded', {status: 400});
    }

    const reader = file.stream().getReader();
    const stream = new ReadableStream({
        async start(controller) {
            try {
                controller.enqueue(encode({status: ETranscribeStatus.PROCESSING}));
                await transcribe(reader, (text: string) => {
                    controller.enqueue(encode({status: ETranscribeStatus.CHUNK, result: text}));
                });
                controller.enqueue(encode({status: ETranscribeStatus.COMPLETED}));
                controller.close();
            } catch (error) {
                controller.enqueue(encode({
                    status: ETranscribeStatus.ERROR,
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
}
