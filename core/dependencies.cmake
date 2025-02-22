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
