API:
Parkour2 parkour2 = (Parkour2) getServer().getPluginManager().getPlugin("mwParkour2");
if (parkour2.getParkoursManager().containsParkour(56)) {
  parkour2.getParkoursManager().getParkour(56).getName();
}
