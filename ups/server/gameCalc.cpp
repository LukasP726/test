//
// Created by qwerty on 26.01.22.
//
#include "gameCalc.h"
/*********
 *
 * game part
 *
 * ******/

void generate_playfield(room *playroom){
    //vyplnim pole neutral uzemim, pote hrac 1 meni sve n, hrac 2 na 2
    for (auto & i : playroom->field) {
        for (int j = 0; j < 11; j++) {
            i[j] = 0;
        }
    }
}

bool ctrl_turn_valid(bool player, int x, int y){
    //mezi klikatelnymi poli
    if((x + y) % 2 == 0){
        return false;
    }

    if (player and ((x % 2) == 0)){
        return true;
    }
    if(!player and ((x % 2) != 0)){
        return true;
    }
    //nejak obsadil pole oponenta
    return false;
}

void proceed_turn(room &r, int x, int y){
    if (r.active_player){
        r.field[x][y] = 1;
        if ((r.field[x + 2][y] == 1) && (r.field[x + 1][y] == 0)){
            r.field[x + 1][y] = 1;
        }
        if ((r.field[x - 2][y] == 1) && (r.field[x - 1][y] == 0)){
            r.field[x - 1][y] = 1;
        }
        if ((r.field[x][y + 2] == 1) && (r.field[x][y + 1] == 0)){
            r.field[x][y + 1] = 1;
        }
        if ((r.field[x][y - 2] == 1) && (r.field[x][y - 1] == 0)){
            r.field[x][y - 1] = 1;
        }
    }
    else{
        r.field[x][y] = 2;
        if ((r.field[x + 2][y] == 2) && (r.field[x + 1][y] == 0)){
            r.field[x + 1][y] = 2;
        }
        if ((r.field[x - 2][y] == 2) && (r.field[x - 1][y] == 0)){
            r.field[x - 1][y] = 2;
        }
        if ((r.field[x][y + 2] == 2) && (r.field[x][y + 1] == 0)){
            r.field[x][y + 1] = 2;
        }
        if ((r.field[x][y - 2] == 2) && (r.field[x][y - 1] == 0)){
            r.field[x][y - 1] = 2;
        }
    }
}

bool find_win_road_existence(room &r, int x, int y, bool player){
    cout << "[" << x << ";" << y << "]" << " - " << player << endl;

    int init_color = r.field[x][y];
    if (((x + 1) <= 10)){
        if (r.field[x + 1][y] == init_color and (!r.field_ctrl[x + 1][y])){
            r.field_ctrl[x + 1][y] = true;
            bool out = find_win_road_existence(r, x + 1, y, player);
            if(out){
                return out;
            }
        }
    }
    if (((x - 1) >= 0)){
        if (r.field[x - 1][y] == init_color and (!r.field_ctrl[x - 1][y])){
            r.field_ctrl[x - 1][y] = true;
            bool out =find_win_road_existence(r, x - 1, y, player);
            if(out){
                return out;
            }
        }
    }
    if (((y + 1) <= 10)){
        if (r.field[x][y + 1] == init_color and (!r.field_ctrl[x][y + 1])){
            r.field_ctrl[x][y + 1] = true;
            bool out =find_win_road_existence(r, x, y + 1, player);
            if(out){
                return out;
            }
        }
    }
    if (((y - 1) >= 0)){
        if (r.field[x][y - 1] == init_color and (!r.field_ctrl[x][y - 1])){
            r.field_ctrl[x][y - 1] = true;
            bool out =find_win_road_existence(r, x, y - 1, player);
            if(out){
                return out;
            }
        }
    }
    if (player and x == 10){
        return true;
    }
    if(!player and y == 10){
        return true;
    }
    return false;
}


bool control_victory(room &r){
    cout << "-----------------------------controlling victory--------------" << endl;
    bool player = r.active_player;
    for (int i = 0; i < 11; i++) {
        for (int j = 0; j < 11; j++) {
            r.field_ctrl[i][j] = false;
        }
    }

    for (int i = 1; i <= 9; i += 2) {
        if(player){
            if (r.field[0][i] == 0){continue;}
            if(find_win_road_existence(r, 0, i, player)){
                cout << "-----------------------------control victory - true--------------" << endl;
                return true;
            }
        }
        else{
            if (r.field[i][0] == 0){continue;}
            if(find_win_road_existence(r, i, 0, player)){
                cout << "-----------------------------control victory - true--------------" << endl;
                return true;
            }
        }
    }
    cout << "-----------------------------control victory - false--------------" << endl;

    return false;
}

/**********
 *
 * game part end
 *
 * ********/
