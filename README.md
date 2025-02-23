# Meeting Summary

This project is an automatic meeting summarization tool that transcribes and summarizes video/audio recordings using [whisper.cpp](https://github.com/ggerganov/whisper.cpp). It processes media files using `ffmpeg`, transcribes them with `whisper`, and provides a gRPC-based API. The frontend interface is built using SvelteKit.

## Features
- [x] Transcribe audio and video files into text
- [ ] (To Be Implemented) Generate meeting summaries from transcripts
- [x] gRPC-based communication between the backend and frontend
- [x] SvelteKit-based web interface for user interaction

Here is a preview of the current working state, which only performs transcription:
![image](https://github.com/user-attachments/assets/5671518b-7bf9-42cd-9328-a8969485b826)


## Installation

The recommended approach is running the meeting summary tool as a docker compose project. You just need to execute the following command in the root directory of the repository:

```sh
docker compose up -d
```

This automatically downloads all required dependencies and starts the backend and frontend. The frontend is then reachable at http://localhost:8080

## Usage
1. Upload an audio or video file via the web interface.
2. The backend processes the media file using `ffmpeg`.
3. `whisper.cpp` transcribes the audio.
4. The transcription is streamed to the web interface.

## Development Prerequisites
Ensure you have the following dependencies installed before building and running the project:

### Dependencies

 - [CMake](https://cmake.org)
 - [LLVM](https://llvm.org)
 - [Ninja](https://ninja-build.org)
 - [nodejs](https://nodejs.org/)
 - [ffmpeg libraries](https://ffmpeg.org)
 - [protobuf](https://protobuf.dev)
 - [grpc](https://grpc.io)

### Additional Requirements
Download a Whisper model before running the project. Models are available at [Whisper.cpp models](https://github.com/ggerganov/whisper.cpp#usage). Example:
```bash
cd core/models
wget https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-tiny.bin
```

Note that this must not be done when the project is started via docker compose.

### Installation
Clone the repository and navigate to the project directory:
```bash
git clone https://github.com/yourusername/automatic-meeting-summarizer.git
cd automatic-meeting-summarizer
```

#### Running the Backend
```bash
cd core
cmake --preset=<os>-64-release
cmake --preset=<os>-64-release --build
./build/<os>-64-release/meeting-summary models/ggml-tiny.bin 50051
```

#### Setting Up the Frontend
```bash
cd frontend
npm install
npm run dev
```
The frontend should now be available at http://localhost:5173.
