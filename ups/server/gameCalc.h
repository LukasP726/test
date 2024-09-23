#ifndef UPS_SERVER_GAMECALC_H
#define UPS_SERVER_GAMECALC_H
#include "servLog.h"

using namespace std;
#define READY 0
#define PLAYER_DISCONECTED 1
#define IN_GAME 2
#define CONNECTED 10
#define DISCONNECTED 11
#define WON 12


//definuj inty pro stavy
struct player {
    string username;
    int client_socket;
    bool player; //player1 - leva-prava, true; player2 - hore-dole, false
    int room_ID;
    int state = CONNECTED;

};


struct room {
    int room_id;
    std::vector<player *> players;
    int state;
    mutex *roomPlayers_mutex;
    bool active_player = true;
    long long last_time_disconnected = -1; //pro reconnect
    int field[11][11];
    bool field_ctrl[11][11];
    bool game_finished = false;
};




//ture - valid; false, invalid
bool ctrl_turn_valid(bool player, int x, int y);
void generate_playfield(room *playroom);
bool ctrl_turn_valid(bool player, int x, int y);
void proceed_turn(room &r, int x, int y);
bool find_win_road_existence(room &r, int x, int y, bool player);

// 0 - nevyhrál, 1 - vyhrál
bool control_victory(room &r);

#endif