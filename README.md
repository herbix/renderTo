Render To
====

RenderTo is a <a href="http://files.minecraftforge.net/">MinecraftForge</a> mod used to generate
well-rendered item, block and entity pictures, These pictures can be used in mod wiki sites, 
presenting mod item, block and mobs, just like <a href="http://minecraft.gamepedia.com/">Official
Minecraft Wiki</a> does.

<h2>Download & Installation</h2>
Latest Release: <a href="https://github.com/herbix/renderTo/raw/master/build/libs/RenderTo-0.1.0.jar">RenderTo-0.1.0.jar</a>

RenderTo can be installed in the same way as other mods by putting "RenderTo-x.x.x.jar" into
"mods" directory.

<h2>Usage</h2>
Create a world in single player, make sure you are in creative mode or cheating is enabled. You
could find a "Render To" item in creative mode inventory, or acquire it by command:
<pre>
/give &lt;your name&gt; renderto:render_to
</pre>
Hold "Render To", right click to open render to GUI.

<img src="intro/gui.png"/>

First, select a mod in left-top list. Then choose what to render by click right-top buttons.
Left-bottom list would refresh if you change mod or render type. Choose an item for rendering in
it and finally a preview is shown in the middle canvas. Click "Save", and the rendered picture
would be stored in a file with following path:
<pre>
.minecraft/RenderTo/&lt;modid&gt;/&lt;render type&gt;/&lt;size&gt;/&lt;item/block/entity name&gt;.png
</pre>

Also, clicking "Save All" would save all items in left-bottom list. It would cost much time and
Minecraft won't response your input during it. Wait for it and it would finish after all.

For item type rendering, change "size" textfield would change the output picture size.

For entity type rendering, the model could rotate and scale. But "size" textfield has no effect.

Block rendering is not finished yet.

<h2>Gallery</h2>

<img src="intro/b1.png"><img src="intro/b2.png"><img src="intro/b3.png"><img src="intro/b4.png">

<img src="intro/e1.png"><img src="intro/e2.png">

<img src="intro/e3.png"><img src="intro/e4.png">

<h2>Develop & Contribute</h2>
You could fork this repository in github, or clone it from github:
<pre>
> git clone https://github.com/herbix/renderTo.git
</pre>
Do same thing as installing MinecraftForge and developing environment is built:
<pre>
> gradlew setupDecompWorkspace eclipse
</pre>

Anyone could contribute to RenderTo by creating a pull request. I would appreciate you if you make
it better.

<h2>License</h2>
RenderTo is in GPL2 license. Read <a href="LICENSE">GPL2</a>.
