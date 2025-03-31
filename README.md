# Summary Generation

This project is an automatic summarization tool that transcribes and summarizes video/audio recordings using [whisper.cpp](https://github.com/ggerganov/whisper.cpp). The summary generation can be performed by an arbitrary OpenAI instance (e.g. self-hosted). It processes media files using `ffmpeg`, transcribes them with `whisper`, and summarizes them with the configured OpenAI instance. The frontend interface is built using SvelteKit and communicates with the backend via `gRPC`.

## Features To Be Implemented

- [ ] OAuth login with GitHub account (elias-plank)
- [ ] Analytics, maybe via OpenAI instance
- [ ] Store previous transcriptions in database (needs backend and frontend)
- [ ] User Settings like additional information that is passed to prompt

### Spring Boot Service For Persistance

The spring boot service should offer REST endpoints for storing and retrieving generated transcripts/summaries. It should also provide REST endpoints for configuring user settings, like additional information that should be provided to the OpenAI instance when a summary is generated. Also maybe the ability to configure the url of the OpenAI instance, at the moment it is hardcoded in the docker compose file.

Responsible Persons:
 - Fabian: backend
 - MBlaim: frontend
 - DefinedSpace: both? for settings

### Spring Boot Service For Analytics

The spring boot service should offer REST endpoints for analysing previous transcriptions/summaries, maybe some statitics. 

Responsible Persons:
 - Patrick: backend
 - Benedikt: frontend

## Features

- [x] Transcribe audio and video files into text
- [x] Generate meeting summaries from transcripts
- [x] gRPC-based communication between the backend and frontend
- [x] SvelteKit-based web interface for user interaction

Here is a preview of the current working state:

![preview](docs/preview.png)

## Installation

> **Note**: Setting up an OpenAI instance can be skipped, I configured a `deepseek-r1:14b` instance on http://engelbert.ip-ddns.com:80

The recommended approach is running the meeting summary tool as a docker compose project. When using the docker compose project, the only thing that needs to be configured is the OpenAI instance that will be used for the summary generation. This can be configured inside the [compose.yaml](./compose.yaml) by altering the `backend` service:

```yaml
services:
  backend:
    ...
    environment:
      - OPENAI_ENDPOINT=http://host.docker.internal:11434
      - OPENAI_TOKEN=""
```

As specified above, the default endpoint is `host.docker.internal:11434`, which just points to the host that runs docker. This configuration works with OpenAI instances that are self-hosted (e.g. via [Ollama](https://ollama.com/)) on the host machine. Alternatives to that are:

 -  Using the official [OpenAI](https://api.openai.com/) or the [DeepSeek](https://api.deepseek.com) API. **Note that this will probably result in costs for you, as these official APIs charge a price per query/tokens**.
 - Using a self-hosted OpenAI instance that runs somewhere else. In this case you just need to set the `OPENAI_ENDPOINT` variable to the host that runs your instance.

The only important thing here is that the OpenAI endpoint hosts an API that conforms to the [OpenAI API reference](https://platform.openai.com/docs/api-reference/introduction).

Once that is configured, you just need to execute the following command in the root directory of the repository:

```sh
docker compose up -d
```

This automatically downloads all required dependencies and starts the backend and frontend. The frontend is then reachable at http://localhost:8080

## Usage

1. Upload an audio or video file via the web interface.
2. The backend processes the media file using `ffmpeg`.
3. `whisper.cpp` transcribes the audio.
4. The transcription is streamed to the web interface.
5. Start a summary for the current transcription.

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
cd backend/models
wget https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-tiny.bin
```

Note that this must not be done when the project is started via docker compose.

### Installation
Clone the repository and navigate to the project directory:
```bash
git clone https://github.com/elias-plank/summary-gen.git
cd summary-gen
```

#### Running the Backend
```bash
cd backend
cmake --preset=<os>-64-release
cmake --preset=<os>-64-release --build
./build/<os>-64-release/summary-gen models/ggml-tiny.bin 50051
```

The backend will now listen for grpc messages at `localhost:50051`

#### Setting Up the Frontend
```bash
cd frontend
npm install
npm run dev
```
The frontend should now be available at http://localhost:5173.
