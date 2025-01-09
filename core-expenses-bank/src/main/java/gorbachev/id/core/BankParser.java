package gorbachev.id.core;

import gorbachev.id.core.model.ParamParser;

import java.io.IOException;

public interface BankParser {

    ResultParser parse(ParamParser params) throws IOException;

    /**
     * How supported extension of files.
     * <p>
     * values should be such as: <b>*.xml, *.html, *.pdf </b> and other
     */
    String[] supportedExtensions();
}
