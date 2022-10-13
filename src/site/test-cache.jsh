var cacheDir = Path.of(System.getProperty("user.home")).resolve("test-cache");
Files.createDirectories(cacheDir);
var file = cacheDir.resolve("s.txt");
if (Files.exists(file)) {
    System.out.println("### FILE EXISTS! ###");
    System.out.println(Files.readString(file));
} else {
    System.out.println("### Create new file ###");
    Files.writeString(file, "abc-xyz");
}

/exit
