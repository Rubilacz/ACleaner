
package ind.rubilacz.tools.acleaner.detector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ind.rubilacz.tools.acleaner.model.Debris;
import ind.rubilacz.tools.acleaner.model.Document;

public abstract class CharactersDetector extends Detector {

    private Pattern mPattern;

    @Override
    protected Set<Debris> catchDebris(File file) {
        if (mPattern == null) {
            mPattern = pattern();
        }
        Set<Debris> debris = new HashSet<Debris>();
        Document doc = new Document(file);
        String content = readContent(file);
        Matcher matcher = mPattern.matcher(content);
        String name;
        while (matcher.find()) {
            name = matcher.group(1);
            if (!mElementNames.contains(name)) {
                debris.add(obtain(name, doc));
            }
        }

        return debris;
    }

    protected abstract Debris obtain(String name, Document container);

    protected Pattern pattern() {
        return Pattern.compile("\\s*<\\s*" + identifier()
                + "\\s*name\\s*=\\s*\"(.*?)\"\\s*>.*</\\s*" + identifier() + "\\s*>+?");
    }

    @Override
    protected boolean shouldDirProcessDeeply(String relativePath) {
        return false;
    }

    private String readContent(File file) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            StringBuilder builder = new StringBuilder();
            char[] buffer = new char[1024];
            int n = 0;
            while ((n = reader.read(buffer)) != -1) {
                builder.append(buffer, 0, n);
            }
            return builder.toString();
        } catch (Exception e) {
            return "";
        } finally {
            try {
                reader.close();
            } catch (Exception e) {
                // ignore
            }
        }
    }

    @Override
    protected String getDebrisDesc() {
        return "totally " + mDebris.size() + " useless " + identifier() + "s.";
    }

    @Override
    protected boolean isDeepProcessingNeeded() {
        return false;
    }

}
