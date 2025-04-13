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
pkg_check_modules(AVFORMAT REQUIRED libavformat)
pkg_check_modules(AVCODEC REQUIRED libavcodec)
pkg_check_modules(AVUTIL REQUIRED libavutil)
pkg_check_modules(SWSCALE REQUIRED libswscale)
pkg_check_modules(SWRESAMPLE REQUIRED libswresample)
find_package(gRPC REQUIRED)
find_package(CURL REQUIRED)

link_directories(
        ${AVFORMAT_LIBRARY_DIRS}
        ${AVCODEC_LIBRARY_DIRS}
        ${AVUTIL_LIBRARY_DIRS}
        ${SWSCALE_LIBRARY_DIRS}
        ${SWRESAMPLE_LIBRARY_DIRS}
)
