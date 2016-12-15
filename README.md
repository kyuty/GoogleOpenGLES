fork from https://github.com/google/grafika

1.AsyncTask:  
AsyncTask enables proper and easy use of the UI thread.
This class allows to perform background operations and publish results on the UI thread `without having to manipulate threads and/or handlers`.

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

AlertDialog.Builder.  
AlertDialog dialog = builder.create();  
dialog.show();

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
