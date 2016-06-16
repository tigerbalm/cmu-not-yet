#pragma once
#ifndef __PIN_DEF__
#define __PIN_DEF__

/* Parking slots' */
#define PARKING_STALL1_LED	22
#define PARKING_STALL2_LED  23
#define PARKING_STALL3_LED  24
#define PARKING_STALL4_LED  25

/* Entry/Exit gates' LED */
#define ENTRY_GATE_GREEN_LED	26
#define ENTRY_GATE_RED_LED		27
#define EXIT_GATE_GREEN_LED		28
#define EXIT_GATE_RED_LED		29

#define GATE_LED_START			ENTRY_GATE_GREEN_LED
#define GATE_LED_END			EXIT_GATE_RED_LED

/* Entry/Exit gate beam */
#define ENTRY_BEAM_RECEIVER  34 
#define EXIT_BEAM_RECEIVER   35
#endif