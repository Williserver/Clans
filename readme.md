# Clans

Clans -- a plugin for user-oriented team management! Users can create and join teams which then optionally connect to Vanilla's team system and other plugins such as LuckPerms.

## Commands

* `/clans help`
    * Pull up a list of Clans commands!
    * *May be run by:* anyone.
* `/clans create (clan name)`
  * Create a new clan under your visionary leadership!
  * *May be run by:* any player who is not already in a clan.
* `/clans crown (new leader's username)`
  * Make a co-leader of your clan the new leader!
  * *May be run by:* clan leader
* `/clans demote (demoted username)`
  * Demote a player in your clan whom you outrank.
  * *May be run by:* any player.
* `/clans disband`
  * Disband your clan.
  * *May be run by:* any player who owns a clan.
* `/clans info (clan name)`
  * Pull up information on a clan with a given name.
  * *May be run by:* anyone.
* `/clans invite (player name)`
  * Invite a member to your clan.
  * *May be run by:* any player with a rank of elder or above in a clan.
* `/clans join (clan name)`
  * Join a clan.
  * *May be run by:* Any player who is not already in a clan.
* `/clans kick (clan name)`
  * Kick a player whom you outrank from your clan.
  * *May be run by:* Any player.
* `/clans leave`
  * Leave your clan.
  * *May be run by:* Any player in a clan.
* `/clans list`
  * List all clans on this server.
  * *May be run by:* Anyone.
* `/clans promote (promoted player name)`
  * Promote a player to the next rank.
  * *May be run by:* Any player in a clan who outranks the promoted player.
* `/clans setPrefix (prefix)`
  * Change the clan's chat prefix.
  * *May be run by:* Any player with rank co-leader or above in some clan.
* `/clans setColor (color name)`
  * Change the clan's chat color.
  * *May be run by:* Any player with rank co-leader or above in some clan.

## Config
* `confirmTime`
  * Maximum time for players to confirm destructive actions (i.e. leaving the clan).
  * If this time expires, the command must be invoked again.
  * Default: 30.
* `scoreboardTeamsIntegration`
  * Whether to integrate with vanilla's team system.
  * If true, scoreboard teams will be created for all clans, and members will be automatically enrolled in them.
  * Default: true
* `luckPermsIntegration`
  * Whether to integrate with the LuckPerms plugin.
  * If true, a LuckPerms track for clans will be added, and players will be added to LuckPerms groups corresponding to their clans.
  * Default: false
* `luckPermsTrackName`
  * If LuckPerms integration is on, the name of the track to put clan groups under.
  * Change this to prevent overlapping track names!
  * Default: "clans"

## Planned Updates
I'm a grad student, so I don't have a ton of time to work on this plugin! Feel free to submit pull requests, and definitely report any bugs to Willmo3.

With that in mind, here are some improvements I have in mind for the future:
* Move to 1.21.8.
* Improve clan prefix feature.
  * Specifically, make a clan's prefix and color separate options, rather than directly appending color codes to the prefix as a string -- this was a quick fix for launch.
* Polish scoreboard teams integration.
* Add clan ally chat.

