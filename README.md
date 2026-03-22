A Paper MC Plugin that prevents players from being targeted or damaged while AFK.
Has configurable timer, messages, prefixes, and suffixes!

LuckPerms or PlaceholderAPI is needed for suffix/prefix.

Default Config:
```yaml
# AFK Protection configuration
afk:
  timer: 120 # seconds

# Use %player% for display name
messages:
  enabled: true
  +afk: "%player% has gone afk"
  -afk: "%player% has returned"

# Below options require LuckPerms
prefix:
  enabled: false
  value: "[AFK]"
  weight: 1

suffix:
  enabled: false
  value: "💤"
  weight: 1

# Value of prefix and suffix can be accessed if PlaceholderAPI is installed.
# ...even is LuckPerms is not installed.
# Placeholders:
#   %afkprotection_prefix%` - configured prefix
#   %afkprotection_suffix%` - configured suffix
#   %afkprotection_isAFK%` - trueValue or trueValue, if executing player is AFK
#   %afkprotection_isAFK_<player>%` - trueValue or trueValue, if player is AFK
#
# - The timer counts down from configured timer. It gets into negative when player is AFK.
#   %afkprotection_countdown%` - afk timer of executing player in milliseconds, counts down from configured timer
#   %afkprotection_countdown_<player>%` - afk timer of player in milliseconds, counts down from configured timer
#   %afkprotection_countdownSeconds%` - afk timer of executing player in seconds, counts down from configured timer
#   %afkprotection_countdownSeconds_<player>%` - afk timer of player in seconds, counts down from configured timer
placeholder:
  trueValue: "true"
  falseValue: "false"
```
