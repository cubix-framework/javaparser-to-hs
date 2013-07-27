package org.DFish.tools.calculator.parser;

import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.DFish.tools.calculator.helper.*;
import org.DFish.tools.calculator.helper.CalculatorOperator;
import org.DFish.tools.calculator.helper.CalculatorParser;
import org.DFish.tools.calculator.info.*;

public class FormulaParser implements CalculatorParser {
  protected CalculatorHandler handler = null;
  protected Set<String> opSet;  
  protected HashMap<String, CalculatorOperator> opMap;
  
  public FormulaParser(CalculatorHandler handler){
    if(handler == null){
      handler = new DefaultHandler();
    } else {
      this.handler = handler;
    }
    
    initOperatorMap();
  }
  
  public FormulaParser(){
    handler = new DefaultHandler();
    
    initOperatorMap();
  }
  
  protected void initOperatorMap(){
    opMap = new HashMap<String, CalculatorOperator>();
    
    // symbols
    opMap.put("+", CalculatorOperator.PLUS);
    opMap.put("-", CalculatorOperator.DECREASE);
    opMap.put("*", CalculatorOperator.MULTIPLY);
    opMap.put("/", CalculatorOperator.DIVIDE);
    opMap.put("%", CalculatorOperator.MODE);
    opMap.put("<<", CalculatorOperator.LEFT_SHIFT);
    opMap.put(">>", CalculatorOperator.RIGHT_SHIFT);
    opMap.put("(", CalculatorOperator.LEFT_BRACKET);
    opMap.put(")", CalculatorOperator.RIGHT_BRACKET);
    opMap.put("&", CalculatorOperator.AND);
    opMap.put("|", CalculatorOperator.OR);
    opMap.put("^", CalculatorOperator.XOR);
    
    // words
    opMap.put("PLUS", CalculatorOperator.PLUS);
    opMap.put("DECREASE", CalculatorOperator.DECREASE);
    opMap.put("MULTIPLY", CalculatorOperator.MULTIPLY);
    opMap.put("DIVIDE", CalculatorOperator.DIVIDE);
    opMap.put("MODE", CalculatorOperator.MODE);
    opMap.put("LEFT_SHIFT", CalculatorOperator.LEFT_SHIFT);
    opMap.put("RIGHT_SHIFT", CalculatorOperator.RIGHT_SHIFT);
    opMap.put("AND", CalculatorOperator.AND);
    opMap.put("XOR", CalculatorOperator.XOR);
    opMap.put("OR", CalculatorOperator.OR);
    
    // functions
    opMap.put("POWER", CalculatorOperator.POWER);
    
    opSet = opMap.keySet();
  }
   
  protected void parseNumber(NumberParserInfo info) throws NumberFormatException{
    String s = String.valueOf(info.source, info.indexBeginCheck, info.source.length - info.indexBeginCheck);
    Matcher m;
    Number num = null;
    String strFind = null;
    
    while(true){
      //// find float number
      m = Pattern.compile("^([0-9]*\\.[0-9]+)").matcher(s);
      info.find = m.find();
      info.base = 10;
      if(info.find){
        strFind = m.group(1);
        info.checkedCharNumber = strFind.length();
        num =Double.parseDouble(strFind);
        
        break;
      }
      
      //// find hex number
      m = Pattern.compile("^0[xX](([aAbBcCdDeEfF]|\\d)+)").matcher(s);
      info.find = m.find();
      info.base = 16;
      if(info.find){
        strFind = m.group(1);
        info.checkedCharNumber = strFind.length() + 2;
        num = Long.parseLong(strFind, info.base);
        
        break;
      }

      //// find octal number
      // due to find out the error we don't use regEx = "^0([0-7]+)";
      m = Pattern.compile("^0([0-9]+)").matcher(s);
      info.find = m.find();
      info.base = 8;
      if(info.find){
        strFind = m.group(1);
        info.checkedCharNumber = strFind.length() + 1;
        num = Long.parseLong(strFind, info.base);
        
        break;
      }

      //// find decimal number
      m = Pattern.compile("^([0-9]+)").matcher(s);
      info.find = m.find();
      info.base = 10;
      if(info.find){
        strFind = m.group(1);
        info.checkedCharNumber = strFind.length();
        num = Long.parseLong(strFind, info.base);
        
        break;
      }
      
      //// find binary number
      // due to find out the error we don't use regEx = "^b([01]+)";
      m = Pattern.compile("^b([0-9]+)").matcher(s);
      info.find = m.find();
      info.base = 2;
      if(info.find){
        strFind = m.group(1);
        info.checkedCharNumber = strFind.length() + 1;
        num = Long.parseLong(strFind, info.base);
        
        break;
      }

      //// find pi = 3.1415926...
      m = Pattern.compile("^(PI)").matcher(s);
      info.find = m.find();
      info.base = 10;
      if(info.find){
        strFind = m.group(1);
        info.checkedCharNumber = 2;
        num = Math.PI;
        
        break;
      }
      
      break;
    }
    
    if(info.find){
      info.number = num;
      
      return;
    }
  }
  
  protected void parseOperator(OperatorParserInfo info) throws UnsupportedOperationException{
    String str = String.valueOf(info.source, info.indexBeginCheck, info.source.length - info.indexBeginCheck);
    String seekString = null;
    
    for(String s : opSet){
      if(str.startsWith(s)){
        seekString = s;
        break;
      }
    }
    
    if(seekString == null){
      info.find = false;
      info.checkedCharNumber = 1;
    } else {
      info.find = true;
      info.checkedCharNumber = seekString.length();
      info.operator = opMap.get(seekString);
    }
    
    return;
  }
  
  //@Override
  public int parse(String formula) {
    if(formula.length() == 0){
      handler.currentPosition(-1, -1);
      handler.fatalError("The length of formula is 0");
      
      return CalculatorParser.PARSER_FATAL_ERROR;
    }
    
    char[] arrayData = formula.toCharArray();
    
    try{
      //// <<- begin parsing
      handler.startFormula(String.copyValueOf(arrayData));
      
      int index;
      char data;
     
      // information
      OperatorParserInfo opInfo = new OperatorParserInfo();
      opInfo.source = arrayData;
      NumberParserInfo numInfo = new NumberParserInfo();
      numInfo.source = arrayData;

      for(index=0; index<arrayData.length; ){
        
        data = arrayData[index];
        
        //// ->> find blank
        switch(data){
          case ' ':
          case '\t':
          case '\r':
          case '\n':
            handler.currentPosition(index, index+1);
            handler.blank(data);
            index++;
            
            continue;
            
            //break;
          default:
            // do nothing
            break;
        }
        //// <<- end of blanks
        
        
        //// ->> find operator
        opInfo.indexBeginCheck = index;

        try {
          parseOperator(opInfo);
        } catch (UnsupportedOperationException ex) {
        
          handler.currentPosition(opInfo.indexBeginCheck, opInfo.indexBeginCheck+opInfo.checkedCharNumber);
          handler.error("Unsupport operator "+data);

          //begin next check;
          index += opInfo.checkedCharNumber;
          continue;
        }
        
        if(opInfo.find){
          handler.currentPosition(index, index+opInfo.checkedCharNumber);
          handler.operator(opInfo.operator);
          
          // begin next chcek
          index += opInfo.checkedCharNumber;
          continue;
        }
        //// <<- end of finding operator
        
        
        //// ->> find number
        numInfo.indexBeginCheck = index;
        
        try{
          parseNumber(numInfo);
        } catch (NumberFormatException e){
          handler.currentPosition(numInfo.indexBeginCheck, numInfo.indexBeginCheck+numInfo.checkedCharNumber);
          handler.error("Cannot format with radix as " + numInfo.base);

           //begin next check;
          index += numInfo.checkedCharNumber;
          continue;
       }
        
        if(numInfo.find){
          handler.currentPosition(index, index+numInfo.checkedCharNumber);
          handler.number(numInfo.number);
          
          //begin next check;
          index += numInfo.checkedCharNumber;
          continue;
        }
        //// <<- end of finding number
      
        
        //// treat others as unknow characters
        handler.currentPosition(index, index+1);
        handler.error("unknow character" + data);
        
        //// next check
        index++;
      }// endof check all characters
      

      //// <<- end of parsing
      handler.endFormula();
      
    } catch (Exception ex) {
      // do nothing
      // ternimate the parsing
      return CalculatorParser.PARSER_TERMINATE;
    }
    
    return CalculatorParser.PARSER_OK;
  }
  
  //@Override
  public void terminate() {
    // do nothing
  }

  //@Override
  public void applyHandler(CalculatorHandler handler) {
    this.handler = handler;
  }
  
  public Set getopMap(){
    return opSet;
  }
}
