cmake_minimum_required(VERSION 3.21)
project(server_final)

set(CMAKE_CXX_STANDARD 14)

add_executable(server_final main.cpp main.h gameCalc.cpp gameCalc.h servLog.cpp servLog.h)

TARGET_LINK_LIBRARIES(server_final pthread)
