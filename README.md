# Campfire XP

## Feature

- Campfire & Soul Campfire can drop XP when killing mobs. It just works<sup>TM</sup>.
- Starting with version 1.1.0, campfire blocks can store xp, facilitating a fully afk xp farm.
  - Shift-right-clicking them with an empty hand shows how much experience is stored.
  - Right-clicking with any stackable non-block items or an empty hand will withdraw/store xp so that your level equals to the stack size of the item.
- 1.2.0 for the new unobfuscated minecraft age, also major mechanism changes:
  - Ownership system that stores xp globally for the player who **places** the campfire.
  - It means that you can access your global xp storage from any campfire you placed. It also means you can destroy and move campfires without losing anything.
  - Standing Right-click to withdraw exactly one level of xp until xp storage is empty.
  - Crouching Right-click to deposit exactly one level of xp until the player has no xp left.

## Trivia

- Campfire sets the playerHitTimer to 100 when damaging the entity.
- It means that you don't have to kill with it, just kill within 5 seconds of colliding with it. 
- You can utilize more efficient damage sources alongside it to design farms.