# Webcam Capture to JavaFX

<p>This project aims to explore the image capture features of a webcam in an 
integrated way with the interface developed in JavaFX. One of the most important 
things here is the implementation of Tasks and Services strategies to reduce or solve
UI interaction concurrent processes problems.<br>It's not so simple to work with 
Platform.runLater when you have a lot of processes working as the same time. It
must be distributed in different structures to avoid errors and UI crash.<br><br>
So I decided to share my sample project to community. Fill free to take a look.</p>

### Tools
<li>Webcam Capture - Sarxos Library</li>
<li>Captured files manipulation - </li>
<li>Java 17+</li>


### Features
<li>Available Webcam list in ComboBox.</li>
<li>Webcam Capture.</li>
<li>Creating .png file from captured image.</li>
<li>UI ImageView update.</li>
<li>UI Data update.</li>
<li>Snapshot of specific UI element using Snapshot method, generating WritableImage and BufferedImage to be consumed.</li>
<li>Generate a mp4 file from webcam captured images.</li>
<li>Generate a mp4 file from snapshot files.</li>

### Classes
<li>WebcamApplication</li>
<p>Main class.</p>
<li>WebcamController</li>
<p>The principal class of this project. Contains all methods and classes to be used.</p>
<li>WebCamPreviewController</li>
<p>An Sarxos sample - <a href="https://github.com/sarxos/webcam-capture/tree/master/webcam-capture-examples/webcam-capture-javafx">webcam-capture-javafx</a> to strategy and performance comparison.</p>