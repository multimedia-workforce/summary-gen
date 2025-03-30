include("${PROJECT_SOURCE_DIR}/cmake/CPM.cmake")

CPMAddPackage(
        NAME whisper
        GITHUB_REPOSITORY ggerganov/whisper.cpp
        VERSION 1.7.4
)

CPMAddPackage(
        NAME spdlog
        GITHUB_REPOSITORY gabime/spdlog
        VERSION 1.12.0
)

CPMAddPackage(
        NAME tlexpected
        GITHUB_REPOSITORY TartanLlama/expected
        VERSION 1.1.0
)

CPMAddPackage(
        NAME nlohmann_json
        GITHUB_REPOSITORY nlohmann/json
        VERSION 3.11.3
)

find_package(PkgConfig REQUIRED)

# Find the FFmpeg libraries via pkg-config
pkg_check_modules(AVFORMAT REQUIRED libavformat)
pkg_check_modules(AVCODEC REQUIRED libavcodec)
pkg_check_modules(AVUTIL REQUIRED libavutil)
pkg_check_modules(SWSCALE REQUIRED libswscale)
pkg_check_modules(SWRESAMPLE REQUIRED libswresample)

# Optionally print found paths
message(STATUS "AVFORMAT include dirs: ${AVFORMAT_INCLUDE_DIRS}")
message(STATUS "AVCODEC include dirs: ${AVCODEC_INCLUDE_DIRS}")
message(STATUS "AVUTIL include dirs: ${AVUTIL_INCLUDE_DIRS}")
message(STATUS "SWSCALE include dirs: ${SWSCALE_INCLUDE_DIRS}")
message(STATUS "SWRESAMPLE include dirs: ${SWRESAMPLE_INCLUDE_DIRS}")

message(STATUS "AVFORMAT library: ${AVFORMAT_LIBRARIES}")
message(STATUS "AVCODEC library: ${AVCODEC_LIBRARIES}")
message(STATUS "AVUTIL library: ${AVUTIL_LIBRARIES}")
message(STATUS "SWSCALE library: ${SWSCALE_LIBRARIES}")
message(STATUS "SWRESAMPLE library: ${SWRESAMPLE_LIBRARIES}")

find_package(Protobuf REQUIRED)

# Display paths
message(STATUS "Protobuf found:")
message(STATUS "  - Protoc: ${Protobuf_PROTOC_EXECUTABLE}")
message(STATUS "  - Include Path: ${Protobuf_INCLUDE_DIRS}")
message(STATUS "  - Libraries: ${Protobuf_LIBRARIES}")

find_package(gRPC REQUIRED)
message(STATUS "gRPC found.")

find_package(CURL REQUIRED)
message(STATUS "CURL found:")
message(STATUS "  - Libraries: ${CURL_LIBRARIES}")
