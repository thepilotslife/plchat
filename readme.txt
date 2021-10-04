Chatbot for the pilot's life SA-MP server.  It connect to its
webchat (which echo's from the game chat) and respond to
commands.  Also saves chatlogs.

Chatlogs captured by the currently running instance are saved
at https://robin.thisisgaming.org/pl/logs/

(This was written in a hurry with the thought "as soon as it
works I'll stop touching it".)

Release topic (contents pasted below):
https://thepilotslife.com/forums/index.php?topic=53440

--------------------------------------------------------------------------------

Hello all

I decided to make a chatbot (kinda like Chappie) for fun and profit to easier
find info about players.

It also records chat logs, which can be found here:
https://robin.thisisgaming.org/pl/logs/ (log timestamps are CET/CEST: GMT+1 or
GMT+2 (DST applies)).
I might add a IRC bridge too, but I feel like I may be the only one that would
use that.

(Note: IRC bridge is added in #pl.echo at irc.liberty-unleashed.co.uk)

commands: !cmds !ping !8ball !player !score !cash !groups !assets !cars
          !houses !licenses !roll !interest !rinterest

!8ball classic 8ball

    <Naldo> !8ball Is Computer Science a awesome degree?
    (WEB) <robin_be> Naldo: Yes, definitely

!player shows a player's score and last online time

    <robin_be> !player Naldo
    (WEB) <robin_be> player Naldo: 10959 score, last seen Online Now

!score shows a player's score and top 3 most popular mission type

    <robin_be> !score Naldo
    (WEB) <robin_be> player Naldo: 10959 score, 9899 missions: 24'/. cargo
                     drop - 18'/. helicopter - 14'/. trucking

!cash shows a player's cash in hands

    <robin_be> !cash Naldo
    (WEB) <robin_be> Naldo has $27,237 in hand

!groups shows the airline and company a player is part of

    <robin_be> !groups Naldo
    (WEB) <robin_be> Naldo is in airline Jetstar Airways and company Star Inc.

!assets shows a player's assets (cars + houses) and values

    <robin_be> !assets robin_be
    (WEB) <robin_be> robin_be has 8 car(s) ($8,400,000) and 2 house(s)
                     ($40,500,000 - 20 slots) for a total of $48,900,000

!cars shows the cars a player owns

    <Naldo> !cars Aaron
    (WEB) <robin_be> Aaron has: Infernus RC Bandit Seasparrow RC Baron
                     Blista Compact RC Goblin RC Goblin AT-400 Phoenix

!houses shows the houses a player owns

    <robin_be> !houses robin_be
    (WEB) <robin_be> North Pole: $35,000,000 15 slots Los Santos Sewers:
                     $5,500,000 5 slots

!licenses shows the licenses a player has

    <robin_be> !licenses robin_be
    (WEB) <robin_be> robin_be has a license for shamal, dodo, maverick, nevada

!roll get a random number (optionall add a number to limit)

    <robin_be> !roll gimme number
    (WEB) <robin_be> robin_be rolls 69

    <TheBestGamerEver> !roll 10
    (WEB) <robin_be> TheBestGamerEver rolls 4

!interest shows the interest you'll get for some amount of money

    <Redirect> !interest 600000
    (WEB) <robin_be> $600,000 will generate about $75 every 60 minutes

!rinterest shows how much money you need to get this amount of interest

    <robin_be> !rinterest 5000
    (WEB) <robin_be> to get $5,000 of interest you need about $40,000,000
