#include "servLog.h"

FILE* log;

/****
 * opens log, where all messages are written
 */
void ready_log(){
    log = fopen("server_log.txt", "w+");
}

/***
* writes in log file
*/
void write_to_log(const std::string& message){
    if(log != nullptr){
        fwrite(message.c_str(), message.size(), 1, log);
        fflush(log);
    }
}