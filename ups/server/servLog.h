//
// Created by qwerty on 26.01.22.
//

#ifndef SERVER_MAIN_TEST_02_SERVLOG_H
#define SERVER_MAIN_TEST_02_SERVLOG_H

#include <vector>
#include <string>
#include <mutex>
#include <iostream>
#include <arpa/inet.h>
#include <mutex>
#include <thread>
#include <unistd.h>
#include <cstring>
#include <condition_variable>
#include <algorithm>
#include <stdlib.h>
#include <csignal>

void ready_log();
void write_to_log(const std::string& message);

#endif //SERVER_MAIN_TEST_02_SERVLOG_H
