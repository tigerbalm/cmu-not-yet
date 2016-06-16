// 
// 
// 

#include "SlotStatusChangeDetector.h"

SlotStatusChangeDetector::SlotStatusChangeDetector(SlotStatusChangeListener * listener)
{
	this->slotStatusChangeListener = listener;

	initSlot();
}

void SlotStatusChangeDetector::initSlot() 
{
	int SLOT_PIN_LIST[MAX_SLOT_COUNT] = { SLOT_PIN_1, SLOT_PIN_2, SLOT_PIN_3, SLOT_PIN_4 };

	for (int i = 0; i < MAX_SLOT_COUNT; i++) 
	{
		slot[i] = new Slot(SLOT_PIN_LIST[i]);
	}
}

void SlotStatusChangeDetector::loop()
{
	for (int i = 0; i < MAX_SLOT_COUNT; i++)
	{
		slot[i]->refresh();
		if (slot[i]->changed())
		{
			slotStatusChangeListener->onSlotChange(i, slot[i]->status());
		}
	}
}