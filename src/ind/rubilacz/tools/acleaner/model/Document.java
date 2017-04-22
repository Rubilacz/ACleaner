
package ind.rubilacz.tools.acleaner.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Document {

    private String mContent;

    private File mDoc;

    public Document(String path) {
        this(new File(path));
    }

    public Document(File resource) {
        if (resource == null || !resource.exists()) {
            throw new IllegalArgumentException("document not exists!");
        }
        mDoc = resource;
    }

    public void eliminate(String content) {
        mHasSaved = false;
        if (mContent == null) {
            inflate();
        }

        mContent = mContent.replaceAll(content, "");
    }

    private boolean mHasSaved = false;

    public void save() {
        if (mHasSaved) {
            return;
        }

        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(mDoc));
            writer.write(mContent);
            writer.close();
        } catch (IOException e) {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e1) {
                    // ignore
                }
            }
        }
        mHasSaved = true;
    }

    File getSource() {
        return mDoc;
    }

    private void inflate() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(mDoc));
            StringBuilder builder = new StringBuilder();
            char[] buffer = new char[1024];
            int n = 0;
            while ((n = reader.read(buffer)) != -1) {
                builder.append(buffer, 0, n);
            }
            mContent = builder.toString();
        } catch (Exception e) {
            mContent = "";
        } finally {
            try {
                reader.close();
            } catch (Exception e) {
                // ignore
            }
        }
    }

}
