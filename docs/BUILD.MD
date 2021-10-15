To build this application, you must have [Git](https://git-scm.com/) installed on your pc. After installing it, open the Git Bash command line in your preferable folder and clone the repository using:

```
git clone https://github.com/DavixDevelop/bte-mover.git
```

After cloning the repository, you have two options. You can build the application, or you can open the project in an IDE like IntelliJ IDEA for development purposes.

## Build the application

Open the cloned repository in the command line, and if you are using Windows, simply execute
```
gradlew.bat build
```

If you are using Linux or macOS you may need to execute
```bash
sudo chmod +x gradlew
```
...with the following afterward
```
./gradlew build
```

After a successful build, open the jar from `build/libs` folder

## Development purposes

Open the cloned repository in the command line. Next, execute `gradlew setupDecompWorkspace` if you are on Windows or `./gradlew setupDecompWorkspace` if you are on Linux/macOS.

Next, if you are using Eclipse, execute `gradlew eclipse` (Windows) or `./gradlew eclipse` (Linux/macOS) then open the project in Eclipse.

If you are using IntelliJ IDEA, import the project by selecting the build.gradle file.

#### Need help or have a question?
You can contact me on Discord, under `davixdevelop#3914`