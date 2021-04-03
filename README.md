# Accura Face Match SDK 

Below steps to setup Accura Face Match SDK's to your project.


#### Step 1: Add files to project assets folder:<br />
```
    Create assets folder under app/src/main and add license file in to assets folder.<br />    
    - accuraface.license // for Accura Face Match <br />
```

#### Step 2 : Implement SDK.
Set Accura Face Match as a dependency to our app level `build.gradle` file.
```
dependencies {
        ...
   // for Accura auto face capture
   implementation project(":autofacedetect")
   // for Accura Face Match
   implementation project(":accura_facem")
}
```
#### Step 3 : Firebase setup
Before executing the project you have to connect it to your Firebase Project.
 - Add google-services.json file to `autofacedetect\` module

#### Step 4 : Implement face match code manually to your activity.

    Important Grant Camera and storage Permission.

    must have to implements FaceCallback, FaceHelper.FaceMatchCallBack to your activity
    FaceHelper faceHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.your_layout);
        // Initialized facehelper in onCreate.
        faceHelper = new FaceHelper(this);

        // Detect face content from database base64 image
        // Store faceContent.getFeature() to your database
        FaceDetectionResult faceContent = faceHelper.getFaceContent(base64Image);

        // pass image for facematch.
        // @params uri1 is for input image
        faceHelper.setInputImage(uri1);
    }

    // Override methods of FaceMatchCallBack
    @Override
    public void onFaceMatch(float score) {
    }

    /**
     * This method execute after {@link FaceHelper#recognizeFace(float[], float[][])}
     *
     * @param score             Face match score
     * @param matchedPosition   matched position with database record
     */
    @Override
    public void onFaceMatch(float score, int matchedPosition) {
      // get face match score
      if (score > 75) {
        	Toast.makeText(this, "MATCHED FOUND", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, "NOT MATCHED", Toast.LENGTH_SHORT).show();
        }
	}

    /**
     * This method execute after {@link FaceHelper#setInputImage(Uri)} and all input image supported methods
     *
     * @param src    Input image as bitmap to display
     */
    @Override
    public void onSetInputImage(Bitmap src1) {

    }

    @Override
    public void onSetMatchImage(Bitmap src2) {

    }

    // Override methods for FaceCallback

    @Override
    public void onInitEngine(int ret) {
    }

    /**
     * This method execute after {@link FaceHelper#setInputImage(Uri)} ()}
     *
     * @param face    face content
     */
    @Override
    public void onLeftDetect(FaceDetectionResult faceResult) {
        faceHelper.recognizeFace(faceResult.getFeature(), dbHelper.getFeatureList());
    }

    //call if face detect
    @Override
    public void onRightDetect(FaceDetectionResult faceResult) {
    }

    @Override
    public void onExtractInit(int ret) {
    }

#### Step 5 : Auto face capture SDK.
Open selfie camera for auto capture.

```
CameraScreenCustomization cameraScreenCustomization = new CameraScreenCustomization();
cameraScreenCustomization.backGroundColor = 0xFFC4C4C5;
cameraScreenCustomization.closeIconColor = 0xFF000000;
cameraScreenCustomization.feedbackBackGroundColor = Color.TRANSPARENT;
cameraScreenCustomization.feedbackTextColor = Color.BLACK;
cameraScreenCustomization.feedbackTextSize = 18;
cameraScreenCustomization.feedBackframeMessage = "Frame Your Face";
cameraScreenCustomization.feedBackAwayMessage = "Move Phone Away";
cameraScreenCustomization.feedBackOpenEyesMessage = "Keep Your Eyes Open";
cameraScreenCustomization.feedBackCloserMessage = "Move Phone Closer";
cameraScreenCustomization.feedBackCenterMessage = "Move Phone Center";
cameraScreenCustomization.feedBackMultipleFaceMessage = "Multiple face detected";
cameraScreenCustomization.feedBackHeadStraightMessage = "Keep Your Head Straight";
cameraScreenCustomization.feedBackLowLightMessage = "Low light detected";
cameraScreenCustomization.feedBackBlurFaceMessage = "Blur detected over face";
cameraScreenCustomization.feedBackGlareFaceMessage = "Glare detected over face";

// Set lightTolerance 0 for full dark document and 100 for full bright document
// if remove low light validation then set lowLightPercentage to -1
cameraScreenCustomization.setLowLightPercentage(39/*lowLightPercentage*/);

// Set blurPercentage 0 to accept clean face and 100 to accept blurry face
// if remove Blur validation then set blurPercentage to -1
cameraScreenCustomization.setBlurPercentage(80/*blurPercentage*/); // To allow blur on face

// Set minGlarePercentage &  maxGlarePercentage to detect glare
// if remove Glare validation then set light minGlarePercentage & maxGlarePercentage to -1
cameraScreenCustomization.setGlarePercentage(6/*glareMinPercentage*/, 98/*glareMaxPercentage*/);

Intent intent = SelfieCameraActivity.SelfieCameraIntent(MainActivity.this,cameraScreenCustomization,Utils.createImageUri(MainActivity.this));
startActivityForResult(intent, CAPTURE_IMAGE);

@Override
protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == RESULT_OK) {
        if (requestCode == CAPTURE_IMAGE && data != null) { // handle request code CAPTURE_IMAGE used for capture image in camera

            Uri uri = data.getData();
            if (uri != null && uri.getPath() != null) {
                File file = new File(uri.getPath()); // retirve image from file path
            }
        }
    }
}
```

## ProGuard
Depending on your ProGuard (DexGuard) config and usage, you may need to include the following lines in your proguards.
```
-keep public class com.inet.facelock.callback.FaceCallback {*;}
-keep public class com.inet.facelock.callback.FaceDetectionResult {*;}
```
