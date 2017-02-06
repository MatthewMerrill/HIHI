package com.mattmerr.hitch.parsetokens;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by merrillm on 2/5/17.
 */
public class ParseNodeBlock extends ParseNode {
    
    private List<ParseNodeStatement> statementList = new ArrayList<>();
    
    public void addStatement(ParseNodeStatement statement) {
        statementList.add(statement);
    }
    
    public List<ParseNodeStatement> getStatementList() {
        return Collections.unmodifiableList(statementList);
    }
}
