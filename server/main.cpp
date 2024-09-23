#include "main.h"

using namespace std;


mutex client_mutex;
mutex room_mutex;
vector<room *> rooms{};
vector<int> connections{};


/*******
 * napise vsem
 * @param message zprava
 */
void broadCast(string message, int sender_socket){
    char buffer[1024];
    strcpy(buffer, message.c_str());
    int len = strlen(message.c_str());
    client_mutex.lock();
    for (int i = 0; i < connections.size(); i++) {
        //aby neposilal sam sobe
        if (connections[i] != sender_socket){
            write_to_log("[BROADCAST] " + message + "from socket [" + to_string(sender_socket) + "]");
            write(connections[i], buffer, len);

        }
    }
    client_mutex.unlock();
}

/********
 * napise konkretnimu clientovi
 * @param message zprava
 * @param client konkretni client
 */
void write_client(string message, int client) {
    char buffer[1024];
    strcpy(buffer, message.c_str());
    write(client, buffer, strlen(message.c_str()));
    if (message != "PING\n" && message != "OK|PING\n") {
        write_to_log("[ TO CLIENT " + to_string(client) + "] : " + message);
        //cout << "written " << message << endl;}
    }
}


/********
 * napise do mistnosti
 * @param message zprava
 * @param notified_room mistnost
 */
void write_room(string message, room *notified_room){
    char buffer[1024];
    for (player *p : notified_room->players) {
        if (p->state != DISCONNECTED) {
            strcpy(buffer, message.c_str());
            write(p->client_socket, buffer, strlen(message.c_str()));
        }
    }
    write_to_log("[ TO ROOM " + to_string(notified_room->room_id) +  "] : " + message);
}
void delete_room(room *roomie){
    room_mutex.lock();
    roomie->roomPlayers_mutex->lock();
    delete roomie->players[0];
    delete roomie->players[1];
    roomie->roomPlayers_mutex->unlock();
    delete roomie->roomPlayers_mutex;
    for (int i = 0; i < rooms.size(); i++) {
        if (rooms[i]->room_id == roomie->room_id){
            //vector<int>::iterator it;
            //it = rooms.begin() + i;
            rooms.erase(std::remove(rooms.begin(), rooms.end(), rooms[i]), rooms.end());
            //rooms.erase(it);
            break;
        }
    }
    //TODO tohle jsi doplnil, kdyby to nefungovalo
    delete roomie;
    room_mutex.unlock();

}
void commence_victory(room *r){
    //cout << "commencing victory" << endl;
    write_room("GAME|END|"+ to_string(!(r->active_player)) + "|WIN\n", r);
    if(r->active_player){
        r->players[1]->state = WON;
    }
    else{
        r->players[0]->state = WON;

    };
    r->game_finished = true;
    sleep(1);
    //delete_room(r);
}

void finish_connection(int socket){
    player *disconected_one;
    int room_disconnected_from = -1;
    close(socket);
    client_mutex.lock();
    for (int i = 0; i < connections.size(); i++) {
        if (connections[i] == socket){
            vector<int>::iterator it;
            it = connections.begin() + i;
            connections.erase(it);
            break;
        }
    }
    client_mutex.unlock();

    room_mutex.lock();
    for (int i = 0; i < rooms.size(); i++) {
        rooms[i]->roomPlayers_mutex->lock();
        for (player *p : rooms[i]->players) {
            if (p->client_socket == socket) {
                p->state = DISCONNECTED;
                disconected_one = p;
                room_disconnected_from = i;
            }
        }
        //pokud se dis z roomky
        if (room_disconnected_from != -1){
            //pokud cekala
            if (rooms[room_disconnected_from]->state == READY){
                if(rooms[room_disconnected_from]-> players[0]->username == disconected_one->username){
                    rooms[room_disconnected_from]-> players.erase(rooms[room_disconnected_from]-> players.begin());

                }
                else if(rooms[room_disconnected_from]-> players[1]->username == disconected_one->username){
                    rooms[room_disconnected_from]-> players.erase(rooms[room_disconnected_from]-> players.begin() + 1);
                }

                delete disconected_one;
                if (rooms[room_disconnected_from]->players.empty()){
                    rooms[room_disconnected_from]->roomPlayers_mutex->unlock();
                    rooms.erase(rooms.begin() + room_disconnected_from);
                    delete rooms[room_disconnected_from];
                }
            }
            else{
                rooms[room_disconnected_from]->state = PLAYER_DISCONECTED;
                rooms[room_disconnected_from]->last_time_disconnected = chrono::duration_cast<chrono::seconds>(chrono::system_clock::now().time_since_epoch()).count();

                for (player *p : rooms[room_disconnected_from]->players) {
                    if (p->state != DISCONNECTED) {
                        write_client("PLAYER_DC|" + disconected_one->username + "\n", p->client_socket);
                    }
                }
            }
        }
        rooms[i]->roomPlayers_mutex->unlock();
    }

    room_mutex.unlock();
}

string timed_reader(int client_socket){
    mutex timeMutex;
    auto *condition = new condition_variable;

    char buffer[1024];
    memset(&buffer, 0, 1024);
    thread t([condition, client_socket, &buffer] {
        int size = read(client_socket, buffer, sizeof(buffer));
        if (size > 0 && size < 1024) {
            condition->notify_one();
        } //else -> timeout
    });
    t.detach();

    unique_lock<std::mutex> l(timeMutex);
    if (condition->wait_for(l, 5s) == cv_status::timeout) {
        delete condition;
        finish_connection(client_socket);
        throw runtime_error("Timeout");
    }
    delete condition;
    string text(buffer);
    write_to_log("[ FROM CLIENT " + to_string(client_socket) +  "] : " + text);

    return text;
}

vector<string> split_message(string message, char determiner) {
    vector<string> mess_out;
    string out = "";
    for(auto x : message){
        if(x == determiner){
            mess_out.push_back(out);
            out = "";
        }
        else{
            out = out + x;
        }
    }
    mess_out.push_back(out);
    return mess_out;
};

string get_rooms_availible(){
    string roomsies = "ROOMS";
    room_mutex.lock();
    //roomsies += rooms.size();
    for (room *roomie : rooms){
        roomsies += "|" + to_string(roomie->room_id) + "|" + to_string(roomie->state) + "|" + to_string(roomie->players.size()) + "|";

        roomsies += roomie->players[0]->username + "$" + to_string(roomie->players[0]->state);
        if (roomie->players.size() == 2){
            roomsies += "|" + roomie->players[1]->username + "$" + to_string(roomie->players[1]->state);

        }
    }
    roomsies += "\n";
    //cout << roomsies <<endl;
    room_mutex.unlock();
    return roomsies;
}

string get_game_state(room *roomie){
    string out = "GAME|STATE|";
    for (int i = 0; i < 11; ++i) {
        for (int j = 0; j < 11; ++j) {
            //cout << roomie->field[i][j] << endl;
            out += std::to_string(roomie->field[i][j]);
            out += "|";
        }
    }
    //roomie->active_player = !(roomie->active_player);
    out += std::to_string(roomie->active_player);
    out += "\n";
    return out;
}

void in_game_loop(int client_socket, room *roomie){
    vector<string> message;
    bool disconnected = false;
    int x, y, i, j;
    string full_message;
    while(!(roomie->game_finished)){
        //cout << "In game loop" << endl;
        if(roomie->last_time_disconnected != -1){
            //cout << "time till recnnect off: " << chrono::duration_cast<chrono::seconds>(chrono::system_clock::now().time_since_epoch()).count() - roomie->last_time_disconnected << endl;
            if(chrono::duration_cast<chrono::seconds>(chrono::system_clock::now().time_since_epoch()).count() - roomie->last_time_disconnected > 60){
                write_client("OPONENTOFF\n", client_socket);
                delete_room(roomie);
                disconnected = true;
                break;
            }
        }
        try{

            full_message = timed_reader(client_socket);

        }
        catch(runtime_error error){
            write_to_log("user disconnected - inGameLoop (socket: " + to_string(client_socket) + ")");
            //cout << "user disconnected - inGameLoop (socket: " << client_socket << ")" << endl;
            return;

        }
        message = split_message(full_message, '|'); //TURN|ROOM_ID|USERNAME|X|Y
        //cout << "gameloop mess: " << full_message << endl;

        if(message[0] == "PING\n" || message[0] == "OK|PING\n" || message[0] == "REFRESH\n"){ //jen pro jistotu...
            write_client("OK|PING\n", client_socket);
            continue;
        }
        if(message[0] == "TURN"){
            //cout << full_message << endl;
            try{
                roomie->room_id = atoi(message[1].c_str());
                x = atoi(message[3].c_str());
                y = atoi(message[4].c_str());
            }
            catch(invalid_argument a){
                write_client("ERROR|TURN|NaN\n", client_socket);
                continue;
            }
            bool found = false;
            for (i = 0; i < rooms.size(); i++) {
                if (rooms[i]->room_id == roomie->room_id){
                    found = true;
                    break;
                }
            }
            if (found){
                found = false;
                if (rooms[i]->players[0]->username == message[2]){
                    if (rooms[i]->active_player == rooms[i]->players[0]->player){
                        if(ctrl_turn_valid(rooms[i]->active_player, x, y)){
                            proceed_turn(*rooms[i], x, y);
                            if (control_victory(*rooms[i])){
                                commence_victory(rooms[i]);
                                break;
                            }
                            else{
                                //TODO - pošli info o proběhlém tahu
                                write_room("OK|TURN|" + to_string(rooms[i]->active_player) + "|" + to_string(x) + "|" + to_string(y) + "|", rooms[i]); //OK|TURN|PLAYER_BOOL|x|y
                                rooms[i]->active_player = !rooms[i]->active_player;
                                //cout << "new player = " << rooms[i]->active_player << endl;
                                continue;
                            }
                        }
                        else{
                            write_client("ERROR|TURN|INVALID_TURN\n", client_socket);
                            continue;
                        }
                    }
                    else{
                        write_client("ERROR|TURN|NOT_PLAYERS_TURN\n", client_socket);
                        continue;
                    }

                }
                else if(rooms[i]->players[1]->username == message[2]){
                    if (rooms[i]->active_player == rooms[i]->players[1]->player){
                        if(ctrl_turn_valid(rooms[i]->active_player, x, y)){
                            proceed_turn(*rooms[i], x, y);
                            if (control_victory(*rooms[i])){
                                commence_victory(rooms[i]);
                                break;
                            }
                            else{
                                //TODO - pošli info o proběhlém tahu
                                write_room("OK|TURN|" + to_string(rooms[i]->active_player) + "|" + to_string(x) + "|" + to_string(y) + "\n", rooms[i]); //OK|TURN|PLAYER_BOOL|x|y
                                rooms[i]->active_player = !rooms[i]->active_player;
                                continue;
                            }
                        }
                        else{
                            write_client("ERROR|TURN|INVALID_TURN\n", client_socket);
                            continue;
                        }
                    }
                    else{
                        write_client("ERROR|TURN|NOT_PLAYERS_TURN\n", client_socket);
                        continue;
                    }
                }
                else{
                    write_client("ERROR|TURN|PLAYER_NOT_IN_ROOM\n", client_socket);
                    continue;
                }
            }
            else{
                write_client("ERROR|TURN|ROOM_NOT_FOUND\n", client_socket);
                continue;
            }
        }
        else{
            write_client("ERROR|UNKNOWN_ORDER\n", client_socket);
            write_to_log("erroring message from " + to_string(client_socket) + " : " + full_message);
            cout << "Erroring message: " << full_message << endl;
            continue;
        }
    }
    if(!disconnected){
        if((roomie->players[0]->client_socket == client_socket && roomie->players[0]->state != WON) || (roomie->players[1]->client_socket == client_socket && roomie->players[1]->state != WON)) {
            delete_room(roomie);
        }
    }
    join_lobby(client_socket);
}

void join_lobby(int client_socket){
    player *new_user;
    write_client(get_rooms_availible(), client_socket);
    room *room_id_to_go;
    while(true){
        //cout << "lobby loop" << endl;
        string message;
        try{

            message = timed_reader(client_socket);

        }
        catch(runtime_error error){
            write_to_log("user disconnected - lobby loop -(socket: " + to_string(client_socket) + ")");
            cout << "user disconnected - lobby loop -(socket: " << client_socket << ")" << endl;
            //continue;
            return;

        }
        //cout << "Message in: " << message << endl;
        vector<string> mess_parted = split_message(message, '|');

        if (mess_parted[0] == "REFRESH\n" || mess_parted[0] == "REFRESH"){
            write_client(get_rooms_availible(), client_socket);
            continue;
        }
        else if(mess_parted[0] == "PING\n" || mess_parted[0] == "PING"){
            write_client("OK|PING\n", client_socket);
            continue;
        }
        else if((mess_parted[0] == "CREATE") && (mess_parted.size() == 2)){//CREATE|username\n
            //cout << "creating room" << endl;
            new_user = new player;
            room_mutex.lock();
            int room_ID = rooms.size();
            string ne = mess_parted[1];
            ne.erase(std::remove_if(ne.begin(), ne.end(), ::isspace), ne.end());
            new_user->username = ne;
            new_user->client_socket = client_socket;
            new_user->player = true;
            new_user->room_ID = room_ID;
            //cout << "done user" << endl;
            room *added_room = new room;
            added_room->players = vector<player *>{new_user};
            added_room->state = READY;
            added_room->roomPlayers_mutex = new mutex;
            added_room->active_player = true;
            added_room->room_id = room_ID;
            //cout << "done room" << endl;
            room_id_to_go = added_room;
            rooms.push_back(added_room);
            generate_playfield(added_room);
            room_mutex.unlock();
            write_client("OK|CREATE|" + to_string(added_room->room_id) + "\n", new_user->client_socket);
            broadCast(get_rooms_availible(), new_user->client_socket);
            //cout << "created" << endl;
            break;
        }
        else if((mess_parted[0] == "JOIN") && (mess_parted.size() == 3)){ //JOIN|ROOM_ID|username (username - jak bych jinak vedeldo se pridal?!)
            int room_ID = -1;
            //cout << "joining" << endl;
            room_ID = atoi(mess_parted[1].c_str());

            //cout << room_ID << endl;
            //cout << "part_mess" << mess_parted[1] << endl;
            if (room_ID == -1){
                write_client("ERROR|JOIN|" + mess_parted[1] + "\n", client_socket);
                continue;
            }
            room_mutex.lock();
            bool found = false;
            int i = 0;
            for (i = 0; i < rooms.size(); i++) {
                if (rooms[i]->room_id == room_ID) {
                    found = true;
                    break;
                }
            }
            bool continuing = false;
            if (found){
                rooms[i]->roomPlayers_mutex->lock();
                if(rooms[i]->state == READY && rooms[i]->players.size() == 1 && rooms[i]->players[0]->username != mess_parted[2]) {
                    new_user = new player;
                    string ne = mess_parted[2];
                    ne.erase(std::remove_if(ne.begin(), ne.end(), ::isspace), ne.end());
                    new_user->username = ne;
                    new_user->client_socket = client_socket;
                    new_user->player = false;
                    new_user->room_ID = rooms[i]->room_id;
                    rooms[i]->players.push_back(new_user);
                    rooms[i]->roomPlayers_mutex->unlock();

                    room_mutex.unlock();
                    broadCast(get_rooms_availible(), new_user->client_socket);
                    write_room("OK|JOIN|" + to_string(rooms[i]->room_id) + "|" + rooms[i]->players[0]->username + "|" + rooms[i]->players[1]->username + "\n", rooms[i]);
                    //cout << "joined" << endl;
                    sleep(1);
                    rooms[i] -> state = IN_GAME;
                    write_room("GAME|START\n", rooms[i]);
                    room_id_to_go = rooms[i];
                    break;
                }
                else {
                    if((rooms[i]->state == READY || rooms[i]->state == PLAYER_DISCONECTED) && ((rooms[i]->players[0]->username + "\n") == mess_parted[2] && rooms[i]->players[0]->state == DISCONNECTED)){
                        //cout << "v reconncetu" << endl;
                        rooms[i]->players[0]->state = CONNECTED;
                        rooms[i]->state = IN_GAME;
                        //cout << "odepisuju ok|reconnect" << endl;
                        write_client("OK|JOIN_RECONNECT|" + to_string(rooms[i]->room_id) + "|" + rooms[i]->players[0]->username + "|" + rooms[i]->players[1]->username + "|"+ get_game_state(rooms[i]), client_socket);
                        //cout << "odepsal jsem" << endl;
                        int del_socket = rooms[i]->players[0]->client_socket;
                        rooms[i]->players[0]->client_socket = client_socket;
                        for (int j = 0; j < connections.size(); ++j) {
                            if (connections[j] == del_socket){
                                vector<int>::iterator it;
                                it = connections.begin() + j;
                                client_mutex.lock();
                                connections.erase(it);
                                client_mutex.unlock();
                                break;

                            }
                        }
                        rooms[i]->roomPlayers_mutex->unlock();
                        rooms[i]->last_time_disconnected = -1;
                        write_client("PLAYER_RECONNECTED\n", rooms[i]->players[1]->client_socket);
                        room_id_to_go = rooms[i];
                        room_mutex.unlock();
                        continuing = true;
                        break;
                    }
                    else if((rooms[i]->state == READY || rooms[i]->state == PLAYER_DISCONECTED) && ((rooms[i]->players[1]->username + "\n") == mess_parted[2] && rooms[i]->players[1]->state == DISCONNECTED)){
                        //cout << "v reconncetu" << endl;
                        rooms[i]->players[1]->state = CONNECTED;
                        rooms[i]->state = IN_GAME;
                        //cout << "odepisuju ok|reconnect" << endl;
                        write_client("OK|JOIN_RECONNECT|" + to_string(rooms[i]->room_id) + "|" + rooms[i]->players[0]->username + "|" + rooms[i]->players[1]->username + "|" + get_game_state(rooms[i]), client_socket);
                        //cout << "odepsal jsem" << endl;
                        int del_socket = rooms[i]->players[1]->client_socket;
                        rooms[i]->players[1]->client_socket = client_socket;
                        for (int j = 0; j < connections.size(); ++j) {
                            if (connections[j] == del_socket){
                                rooms[i]->players[1]->client_socket = client_socket;
                                vector<int>::iterator it;
                                it = connections.begin() + j;
                                client_mutex.lock();
                                connections.erase(it);
                                client_mutex.unlock();
                                break;

                            }
                        }
                        rooms[i]->roomPlayers_mutex->unlock();
                        rooms[i]->last_time_disconnected = -1;
                        write_client("PLAYER_RECONNECTED\n", rooms[i]->players[0]->client_socket);
                        room_id_to_go = rooms[i];
                        room_mutex.unlock();
                        continuing = true;
                        break;
                    }
                    else if(rooms[i]->players[0]->username == mess_parted[2]){
                        rooms[i]->roomPlayers_mutex->unlock();
                        room_mutex.unlock();
                        write_client("ERROR|JOIN|SAME_NAME\n", client_socket);
                        continue;
                    }
                    else{
                        rooms[i]->roomPlayers_mutex->unlock();
                    }

                }
            }
            room_mutex.unlock();
            if (continuing){
                break;
            }
            //cout << rooms[i]->state << endl;
            //cout << mess_parted[2] << endl;
            //cout << rooms[i]->players[1]->username << endl;
            write_client("ERROR|JOIN|" + mess_parted[1] + "\n", client_socket);
            continue;
        }
        else if (mess_parted[0] == "RECONNECT" && (mess_parted.size() == 3)){ //RECONNECT|USERNAME|ROOM_ID
            bool reconnected = false;
            bool player_not_found = false;
            bool room_found = false;
            int room_ID = -1;
            try{
                room_ID = atoi(mess_parted[2].c_str());
            }
            catch(...){
                write_client("ERROR|RECONNECT|ROOM_NOT_FOUND\n", client_socket);
                //continue;
                return;
            }
            if(room_ID < 0){
                write_client("ERROR|RECONNECT|ROOM_NOT_FOUND\n", client_socket);
                //continue;
                return;
            }

            for (int i = 0; i < rooms.size(); i++) {
                if (rooms[i]->room_id == room_ID){
                    room_found = true;
                    //cout << "RECONNECTING!" << endl;
                    if((rooms[i]->players[0]->username == mess_parted[1]) && (rooms[i]->players[0]->state == DISCONNECTED)){
                        //cout << "PLAYER_FOUND!!!" << endl;
                        room_mutex.lock();
                        room *room_used = rooms[i];
                        write_client("OK|RECONNECT|" + get_game_state(room_used), client_socket);
                        rooms[i]->roomPlayers_mutex->lock();
                        rooms[i]->players[0]->state = CONNECTED;
                        rooms[i]->state = IN_GAME;
                        int del_socket = rooms[i]->players[0]->client_socket;
                        rooms[i]->players[0]->client_socket = client_socket;
                        for (int j = 0; j < connections.size(); ++j) {
                            if (connections[j] == del_socket){
                                vector<int>::iterator it;
                                it = connections.begin() + j;
                                client_mutex.lock();
                                connections.erase(it);
                                client_mutex.unlock();
                                break;

                            }
                        }
                        rooms[i]->roomPlayers_mutex->unlock();
                        rooms[i]->last_time_disconnected = -1;
                        write_client("PLAYER_RECONNECTED\n", rooms[i]->players[1]->client_socket);
                        room_id_to_go = rooms[i];
                        room_mutex.unlock();
                        break;

                    }
                    else if((rooms[i]->players[1]->username == mess_parted[1]) && (rooms[i]->players[1]->state == DISCONNECTED)){
                        room_mutex.lock();
                        room *room_used = rooms[i];
                        write_client("OK|RECONNECT|" + get_game_state(room_used), client_socket);
                        rooms[i]->roomPlayers_mutex->lock();
                        rooms[i]->players[1]->state = CONNECTED;
                        rooms[i]->state = IN_GAME;
                        int del_socket = rooms[i]->players[1]->client_socket;
                        rooms[i]->players[1]->client_socket = client_socket;
                        for (int j = 0; j < connections.size(); ++j) {
                            if (connections[j] == del_socket){
                                vector<int>::iterator it;
                                it = connections.begin() + j;
                                client_mutex.lock();
                                connections.erase(it);
                                client_mutex.unlock();
                                break;

                            }
                        }
                        rooms[i]->roomPlayers_mutex->unlock();
                        rooms[i]->last_time_disconnected = -1;
                        write_client("PLAYER_RECONNECTED\n", rooms[i]->players[0]->client_socket);
                        room_id_to_go = rooms[i];
                        room_mutex.unlock();
                        break;
                    }
                    else{
                        player_not_found = true;
                        write_client("ERROR|RECONNECT|PLAYER_NOT_FOUND\n", client_socket);
                        //TODO - ehm...mozna promysli...
                        continue;
                        //return;
                    }
                }
            }

            if (player_not_found){
                continue;
            }
            if(!room_found){
                write_client("ERROR|RECONNECT|ROOM_NOT_FOUND\n", client_socket);
                //TODO - když se mu to libit nebude....tady dodelej continue, v klientovi forceToLobby()
                //continue;
                return;
            }
            if (room_found && !player_not_found){
                break;
            }
        }
    }
    //cout << "Going to game loop" << endl;
    in_game_loop(client_socket, room_id_to_go);
}



int main(int argc, char const *argv[]) {
    int server_socket, client_socket;
    struct sockaddr_in server_addr{}, client_addr{};
    int len;
    int port;
    int return_value;

    if (argc >= 2) {
        try {
            port = stoi(argv[1]);
        }
        catch (...) {
            port = 10005;
        }
    }
    else {
        port = 10005;
    }


    server_socket = socket(AF_INET, SOCK_STREAM, 0);
    if (server_socket < 0) {
        cerr << "SOCKET CREATING ERR" << endl;
        exit(-1);
    }

    int reuse = 1;
    if (setsockopt(server_socket, SOL_SOCKET, SO_REUSEADDR, (const char*)&reuse, sizeof(reuse)) < 0) {
        cerr << "SO_REUSEADDR failed!" << endl;
        exit(5);
    }

#ifdef SO_REUSEPORT
    if (setsockopt(server_socket, SOL_SOCKET, SO_REUSEPORT, (const char*)&reuse, sizeof(reuse)) < 0) {
        cerr << "SO_REUSEPORT failed!" << endl;
        exit(6);
    }
#endif


    server_addr.sin_family = AF_INET;

    if(argc >= 3){
        try{
            server_addr.sin_addr.s_addr = inet_addr(argv[2]);
        }
        catch(exception){
            server_addr.sin_addr.s_addr = INADDR_ANY;
        }
    }
    else{
        server_addr.sin_addr.s_addr = INADDR_ANY;
    }
    server_addr.sin_port = htons(port);

    return_value = bind(server_socket, (struct sockaddr *)&server_addr, sizeof(struct sockaddr_in));
    signal(SIGPIPE, SIG_IGN);
    if (return_value < 0){
        cerr << "SOCKET BINDING ERR" << endl;
        exit(-2);
    }

    return_value = listen(server_socket, 100);
    if(return_value < 0){
        cerr << "SOCKET LISTENING ERR" << endl;
        exit(-3);
    }


    cout << "Waiting for clients on : " << server_addr.sin_addr.s_addr << ":" << to_string(port) << endl;
    ready_log();
    while(true){
        int sizeof_client = sizeof(client_addr);
        client_socket = accept(server_socket, (struct sockaddr *)&client_addr, (socklen_t *) &client_addr);

        if (client_socket < 0){
            cerr << "SOCKET ACCEPT ERR" <<endl;
            exit(-4);
        }
        string client_ip = inet_ntoa(client_addr.sin_addr);


        client_mutex.lock();
        connections.push_back(client_socket);
        client_mutex.unlock();

        thread the_threat(join_lobby, client_socket);
        the_threat.detach();
    }
    return 0;
}
