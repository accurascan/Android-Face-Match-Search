# Accura Face Match SDK 

Below steps to setup Accura Face Match SDK's to your project.


#### Step 1: Add files to project assets folder:<br />
```
    Create assets folder under app/src/main and add license file in to assets folder.<br />    
    - accuraface.license // for Accura Face Match <br />
    
```

#### Step 2 : Implement face match code manually to your activity.

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
        faceHelper.recognizeFace(face.getFeature(), dbHelper.getFeatureList());
    }

    //call if face detect
    @Override
    public void onRightDetect(FaceDetectionResult faceResult) {
    }

    @Override
    public void onExtractInit(int ret) {
    }

## ProGuard
Depending on your ProGuard (DexGuard) config and usage, you may need to include the following lines in your proguards.
```
-keep public class com.inet.facelock.callback.FaceCallback {*;}
-keep public class com.inet.facelock.callback.FaceDetectionResult {*;}
```
