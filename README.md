RubyLive is a dynamic and engaging Minecraft plugin designed for server streamers and administrators who want to add an exciting live spectating experience to their gameplay. With RubyLive, players can easily start and manage live sessions, allowing them to spectate other players in real-time while ensuring smooth chunk loading and an immersive viewing experience.

Features:

Start Live Sessions: Use the /startlive command to begin a live session where you can spectate a random player on the server. Perfect for streaming or moderating gameplay.

Spectate with Smooth Chunk Loading: Unlike traditional spectator mode, RubyLive ensures that chunks are loaded correctly by periodically teleporting the spectator's character above the target player, providing a seamless and uninterrupted viewing experience.

Follow Any Player: The /live command allows any player to call the live controller to spectate them. The plugin dynamically moves the spectator to keep up with the action, ensuring they’re always at the heart of the gameplay.

Action Bar Notifications: The plugin displays action bar messages to inform the spectator of who they are currently following, enhancing the live experience with real-time updates.

Cooldown Management: The /live command comes with a 15-minute cooldown to prevent spamming and ensure fair usage among players.

Permission-Based Access: The /startlive command requires specific permissions (rubylive.startlive), ensuring only authorized players can start live sessions.

Stop Live Sessions: End the live session anytime with the /stoplive command, returning the spectator back to their normal gameplay mode.
Commands:

/startlive: Begins a live session by selecting a random player to spectate. (Requires permission: rubylive.startlive)

/live: Moves the live controller to spectate the player who executed the command. Comes with a 15-minute cooldown.

/stoplive: Stops the current live session and returns the live controller to their previous game mode.
Permissions:

rubylive.startlive: Allows the player to use the /startlive command to start a live session.
How It Works:

Starting a Live Session: A player with the required permission uses /startlive. This command sets the player in spectator mode and begins spectating a random player on the server.

Following Players with /live: Any player on the server can use /live to call the live controller to their location, allowing them to be the center of attention.

Stop Live Sessions: At any point, the live session can be stopped using /stoplive, which safely returns the spectator to their normal game mode.
Ideal For:

Streamers and Content Creators: Add an interactive element to your streams by dynamically following players during live sessions.

Server Moderators: Monitor player activities more efficiently with live spectating, providing real-time oversight without having to manually teleport or navigate.

Community Events: Enhance server events by allowing a designated spectator to follow key players, showcasing the most exciting moments to all participants.
Installation:

Download the RubyLive plugin .jar file.
Place it in your server's plugins folder.
Restart your server.
Configure permissions as needed using your permission management system.
