fork from https://github.com/google/grafika

1.AsyncTask:  
AsyncTask enables proper and easy use of the UI thread.
This class allows to perform background operations and publish results on the UI thread `without having to manipulate threads and/or handlers`.
    
    @WorkerThread @Override // async task thread
    protected Integer doInBackground(Void... params)
    
    @WorkerThread
    protected final void publishProgress(Progress... values)
    
    @MainThread @Override // UI thread
    protected void onProgressUpdate(Integer... progressArray)
    
    @MainThread @Override // UI thread
    protected void onPostExecute(Integer result) 
    
    @MainThread
    public final AsyncTask<Params, Progress, Result> execute(Params... params)

2.java Comparator

    /**
     * Compares two list items.
     */
    private static final Comparator<Map<String, Object>> TEST_LIST_COMPARATOR =
            new Comparator<Map<String, Object>>() {
        @Override
        public int compare(Map<String, Object> map1, Map<String, Object> map2) {
            String title1 = (String) map1.get(TITLE);
            String title2 = (String) map2.get(TITLE);
            return title1.compareTo(title2);
        }
    };
    
    Collections.sort(list, TEST_LIST_COMPARATOR);

3.java 反射

    Intent intent = new Intent();
    // Do the class name resolution here, so we crash up front rather than when the
    // activity list item is selected if the class name is wrong.
    try {
        Class cls = Class.forName("com.android.grafika." + test[2]);
        intent.setClass(this, cls);
    } catch (ClassNotFoundException cnfe) {
        throw new RuntimeException("Unable to find " + test[2], cnfe);
    }

4.One-time singleton initialization;  
若有大量的对该对象的操作，记得加锁。  

    // Housekeeping.
    private static final Object sLock = new Object();
    private static ContentManager sInstance = null;
    public static ContentManager getInstance() {
        synchronized (sLock) {
            if (sInstance == null) {
                sInstance = new ContentManager();
            }
            return sInstance;
        }
    }

5.show dialog.  

    /**
     * Posts an error dialog, including the message from the failure exception.
     */
    private void showFailureDialog(Context context, RuntimeException failure) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.contentGenerationFailedTitle);
        String msg = context.getString(R.string.contentGenerationFailedMsg,
                failure.getMessage());
        builder.setMessage(msg);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    ==========================================================================================
    /**
     * Prepares an alert dialog builder, using the work_dialog view.
     * <p>
     * The caller should finish populating the builder, then call AlertDialog.Builder#show().
     */
    public static AlertDialog.Builder create(Activity activity, int titleId) {
        View view;
        try {
            view = activity.getLayoutInflater().inflate(R.layout.work_dialog, null);
        } catch (InflateException ie) {
            Log.e(TAG, "Exception while inflating work dialog layout: " + ie.getMessage());
            throw ie;
        }

        String title = activity.getString(titleId);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setView(view);
        return builder;
    }
    
    AlertDialog.Builder builder = WorkDialog.create(caller, R.string.preparing_content);
    builder.setCancelable(false);
    AlertDialog dialog = builder.show();

6.xml
    android:fadeScrollbars="false"  
    不淡出滚动条  
    
7.经常修改的变量，被不同线程访问的变量，置成volatile  

    // May be set/read by different threads.
    private volatile boolean mIsStopRequested;

8.  
EglCore.java.  // Core EGL state (display, context, config).  
EGL14 api:  
eglGetDisplay  eglInitialize  eglCreateContext  eglQueryContext  eglChooseConfig  
eglDestroySurface  eglCreateWindowSurface  
eglGetCurrentSurface  eglGetCurrentContext  
eglQueryString  
eglGetError  

    
    // Object win: instanceof android.view.SurfaceView, 
    //              android.view.Surface or android.view.SurfaceHolder
    EGLSurface eglCreateWindowSurface(EGLDisplay dpy,EGLConfig config,Object win,int[] attrib_list,int offset)

    // Creates an EGL surface associated with an offscreen buffer
    eglCreatePbufferSurface  
    
    // Makes our EGL context current, using the supplied surface for both "draw" and "read".
    eglMakeCurrent(EGLDisplay dpy,EGLSurface draw,EGLSurface read,EGLContext ctx)  
    
    // Calls eglSwapBuffers.  Use this to "publish" the current frame.
    swapBuffers(EGLSurface eglSurface)
    
    // Sends the presentation time stamp to EGL.  Time is expressed in nanoseconds.
    eglPresentationTimeANDROID(EGLDisplay dpy,EGLSurface sur,long time)

9.
EglSurfaceBase.java  

使用glReadPixels，将surface保存成一张图片  
    
    /**
     * Saves the EGL surface to a file.
     * <p>
     * Expects that this object's EGL surface is current.
     */
    public void saveFrame(File file) throws IOException {
        if (!mEglCore.isCurrent(mEGLSurface)) {
            throw new RuntimeException("Expected EGL context/surface is not current");
        }
    
        // glReadPixels fills in a "direct" ByteBuffer with what is essentially big-endian RGBA
        // data (i.e. a byte of red, followed by a byte of green...).  While the Bitmap
        // constructor that takes an int[] wants little-endian ARGB (blue/red swapped), the
        // Bitmap "copy pixels" method wants the same format GL provides.
        //
        // Ideally we'd have some way to re-use the ByteBuffer, especially if we're calling
        // here often.
        //
        // Making this even more interesting is the upside-down nature of GL, which means
        // our output will look upside down relative to what appears on screen if the
        // typical GL conventions are used.
    
        String filename = file.toString();
    
        int width = getWidth();
        int height = getHeight();
        ByteBuffer buf = ByteBuffer.allocateDirect(width * height * 4);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        GLES20.glReadPixels(0, 0, width, height,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buf);
        GlUtil.checkGlError("glReadPixels");
        buf.rewind();
    
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(filename));
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bmp.copyPixelsFromBuffer(buf);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, bos);
            bmp.recycle();
        } finally {
            if (bos != null) bos.close();
        }
        Log.d(TAG, "Saved " + width + "x" + height + " frame as '" + filename + "'");
    }

10.
Texture2DProgram.java

    // 顶点着色器 
    // Simple vertex shader, used for all programs.
    private static final String VERTEX_SHADER =
            "uniform mat4 uMVPMatrix;\n" +
            "uniform mat4 uTexMatrix;\n" +
            "attribute vec4 aPosition;\n" +
            "attribute vec4 aTextureCoord;\n" +
            "varying vec2 vTextureCoord;\n" +
            "void main() {\n" +
            "    gl_Position = uMVPMatrix * aPosition;\n" +
            "    vTextureCoord = (uTexMatrix * aTextureCoord).xy;\n" +
            "}\n";

    // 普通的片元着色器
    // Simple fragment shader for use with "normal" 2D textures.
    private static final String FRAGMENT_SHADER_2D =
            "precision mediump float;\n" +
            "varying vec2 vTextureCoord;\n" +
            "uniform sampler2D sTexture;\n" +
            "void main() {\n" +
            "    gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
            "}\n";
    
    // 使用samplerExternalOES
    // Simple fragment shader for use with external 2D textures (e.g. what we get from
    // SurfaceTexture).
    private static final String FRAGMENT_SHADER_EXT =
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;\n" +
            "varying vec2 vTextureCoord;\n" +
            "uniform samplerExternalOES sTexture;\n" +
            "void main() {\n" +
            "    gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
            "}\n";
    
    // texture变黑白
    // Fragment shader that converts color to black & white with a simple transformation.
    private static final String FRAGMENT_SHADER_EXT_BW =
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;\n" +
            "varying vec2 vTextureCoord;\n" +
            "uniform samplerExternalOES sTexture;\n" +
            "void main() {\n" +
            "    vec4 tc = texture2D(sTexture, vTextureCoord);\n" +
            "    float color = tc.r * 0.3 + tc.g * 0.59 + tc.b * 0.11;\n" +
            "    gl_FragColor = vec4(color, color, color, 1.0);\n" +
            "}\n";
    
    
    // 1.滤镜shader 
    // 2.shader里有坐标判断，不提倡写if else，但可以学习一下shader里的坐标判断。
    //
    // Fragment shader with a convolution filter.  The upper-left half will be drawn normally,
    // the lower-right half will have the filter applied, and a thin red line will be drawn
    // at the border.
    //
    // This is not optimized for performance.  Some things that might make this faster:
    // - Remove the conditionals.  They're used to present a half & half view with a red
    //   stripe across the middle, but that's only useful for a demo.
    // - Unroll the loop.  Ideally the compiler does this for you when it's beneficial.
    // - Bake the filter kernel into the shader, instead of passing it through a uniform
    //   array.  That, combined with loop unrolling, should reduce memory accesses.
    public static final int KERNEL_SIZE = 9;
    private static final String FRAGMENT_SHADER_EXT_FILT =
            "#extension GL_OES_EGL_image_external : require\n" +
            "#define KERNEL_SIZE " + KERNEL_SIZE + "\n" +
            "precision highp float;\n" +
            "varying vec2 vTextureCoord;\n" +
            "uniform samplerExternalOES sTexture;\n" +
            "uniform float uKernel[KERNEL_SIZE];\n" +
            "uniform vec2 uTexOffset[KERNEL_SIZE];\n" +
            "uniform float uColorAdjust;\n" +
            "void main() {\n" +
            "    int i = 0;\n" +
            "    vec4 sum = vec4(0.0);\n" +
            "    if (vTextureCoord.x < vTextureCoord.y - 0.005) {\n" +
            "        for (i = 0; i < KERNEL_SIZE; i++) {\n" +
            "            vec4 texc = texture2D(sTexture, vTextureCoord + uTexOffset[i]);\n" +
            "            sum += texc * uKernel[i];\n" +
            "        }\n" +
            "        sum += uColorAdjust;\n" +
            "    } else if (vTextureCoord.x > vTextureCoord.y + 0.005) {\n" +
            "        sum = texture2D(sTexture, vTextureCoord);\n" +
            "    } else {\n" +
            "        sum.r = 1.0;\n" +
            "    }\n" +
            "    gl_FragColor = sum;\n" +
            "}\n";
            
            
    // mTexOffset's length is KERNEL_SIZE * 2
    // size is not KERNEL_SIZE * 2 !!!!!!
    GLES20.glUniform2fv(muTexOffsetLoc, KERNEL_SIZE, mTexOffset, 0);


FlatShadedProgram.java  

    private static final String VERTEX_SHADER =
            "uniform mat4 uMVPMatrix;" +
            "attribute vec4 aPosition;" +
            "void main() {" +
            "    gl_Position = uMVPMatrix * aPosition;" +
            "}";

    private static final String FRAGMENT_SHADER =
            "precision mediump float;" +
            "uniform vec4 uColor;" +
            "void main() {" +
            "    gl_FragColor = uColor;" +
            "}";

============================================================================================

read sequence  

1.MainActivity  
2.ContentManager  
3.WorkDialog  
4.AsyncTask  
5.GeneratedMovie  
6.GLUtil  
7.EglCore  
8.EglSurfaceBase  
9.OffscreenSurface  
10.Drawable2d  
11.Sprite2d  
12.FlatShadedProgram  
13.Texture2dProgram  
14.WindowSurface  
15.FullFrameRect  
16.GeneratedTexture  
17.
3.MoviePlayer  
    视频的解析，用的MediaExtractor，MediaCodec。  
    MediaCodec.releaseOutputBuffer方法会将buffer画在指定的Surface上。  
    内部静态类PlayTask，implement Runnable。  
    Object.wait()会将当前进程wait，直到调用Object.notify() or Object.notifyAll()  
    Handle可以认为是一个线程。  

