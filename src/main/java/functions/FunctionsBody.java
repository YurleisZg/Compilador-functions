/**
 * @authors
 * Wilson Jimenez
 * Kevin Carmona
 * Yurleis Zuluaga
 * Greison Castilla
 * Andrés Quintana
 */
package functions;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.regex.Pattern;

public class FunctionsBody {

    private List<String> functionsNames = new ArrayList<>();
    private Queue<String> errorQueue = new LinkedList<>();
    private boolean mainFlag = false;
    private boolean flag = false;
    public String body(String chain, boolean blockFlag) {
        FunctionsUtils v = new FunctionsUtils();
        CallFunction c = new CallFunction();
        boolean error = false;
        String headerExpresion = "\\s*(FUNCION(\\s+[a-zA-Z_][a-zA-Z0-9_]*\\s*)([(]((\\s*)|((\\s*)(BOOLEANO|ENTERO|FLOTANTE|CARACTER)\\s+[a-zA-Z_][a-zA-Z0-9_]*(\\s*\\[\\s*([0-9]+)*\\s*\\]\\s*)*)((\\s*)[,](\\s*)((BOOLEANO|ENTERO|FLOTANTE|CARACTER)\\s+[a-zA-Z_][a-zA-Z0-9_]*(\\s*\\[\\s*([0-9]+)*\\s*\\]\\s*|\\s*)))*)(\\s*)[)])\\s*:\\s*(BOOLEANO|ENTERO|FLOTANTE|CARACTER|VACIO)\\s*)|(INICIO\\s*)";
        String bodyExpresion = "(\\w|\\W)*|(RETORNA\\s+((([a-zA-Z_][a-zA-Z0-9_]*|[0-9]+)\\s*(\\s*([\\||]|[\\&&]|[+]|[-]|[*]|[/]|[%])\\s*([a-zA-Z_][a-zA-Z0-9_]*|[0-9]+))*)\\s*))*";
        String footerExpresion = "\\s*FIN (FUNCION|INICIO)\\s*";
        String result = "";
        String[] splitChain = null;
        List<String> newValue = new ArrayList<>();
        boolean returnFlag = false;
        boolean paramFlag = false;
        
        if (!chain.isBlank()) {
            if (Pattern.compile(headerExpresion).matcher(chain).find()) {
                splitChain = chain.trim().split("[ (),:]");
                if (splitChain[0].equals("FUNCION")) {
                    error = v.isType(splitChain[splitChain.length - 1]);
                    if (error) {
                        flag = v.types(splitChain[splitChain.length - 1]) == "void" ? true : false;
                        newValue.add(v.types(splitChain[splitChain.length - 1]));
                        newValue.add(" ");
                        newValue.add(splitChain[1] + "(");
                        
                        for (int i = 2; i < splitChain.length; i++) {
                            if (v.isType(splitChain[i]) && i == splitChain.length - 1) {
                                break;
                            }
                            if (i != splitChain.length && v.isType(splitChain[i]) && paramFlag) {
                                newValue.add(", ");
                            }
                            newValue.add(v.types(splitChain[i]));
                            if (v.isType(splitChain[i])) {
                                newValue.add(" ");
                                paramFlag = true;
                            }
                        }
                        newValue.add("){\n");
                    }
                } else if (splitChain[0].equals("INICIO")) {
                    newValue.add("\nint main(){\n");
                    mainFlag = true;
                    flag = false;
                }
            } else if (Pattern.compile(bodyExpresion).matcher(chain).find() && !Pattern.compile(footerExpresion).matcher(chain).find()) {
                splitChain = chain.trim().split(" ");
                for (int k = 0; k < splitChain.length; k++) {
                    if (k == 0 && blockFlag) {
                        newValue.add("\t");
                    }
                    if (splitChain[k].equals("FUNCION") && k == 0) {
                        errorQueue.offer("It's not possible create a function inside another function");
                    }
                    if (splitChain[k].indexOf("(") == 0) {
                        if (c.call(splitChain[k - 1], functionsNames).equals("Syntax Error")) {
                            errorQueue.offer("This function doesn't exist");
                        }
                    } else if (splitChain[k].indexOf("(") != -1) {
                        String auxChain = splitChain[k].substring(0, splitChain[k].indexOf("("));
                        if (c.call(auxChain, functionsNames).equals("Syntax Error")) {
                            errorQueue.offer("This function doesn't exist");
                        }
                    }

                    if (v.checkReturn(splitChain[k])) {
                        if (!flag) {
                            returnFlag = true;
                            newValue.add("return ");
                            continue;
                        } else {
                            errorQueue.offer("A void function can't return any value");
                        }
                    }
                    if (!returnFlag) {
                        newValue.add(splitChain[k]);
                        if (splitChain[k].charAt(splitChain[k].length() - 1) == ')' && k == splitChain.length - 1) {
                            newValue.add(";");
                        } else {
                            newValue.add(" ");
                        }
                    } else {
                        newValue.add(splitChain[k]);
                        if (k == splitChain.length - 1) {
                            newValue.add(";");
                        } else {
                            newValue.add(" ");
                        }
                    }
                }
                newValue.add("\n");
            }
            if (Pattern.compile(footerExpresion).matcher(chain).find()) {
                splitChain = chain.trim().split(" ");
                if ((splitChain[splitChain.length - 1].equals("FUNCION") || splitChain[splitChain.length - 1].equals("INICIO")) && blockFlag) {
                    newValue.add("}");
                } else if (splitChain[splitChain.length - 1].equals("FUNCION") || splitChain[splitChain.length - 1].equals("INICIO")) {
                    errorQueue.offer("Keyword \"FIN FUNCION\" has been found without a keyword \"FUNCION\" before of it");
                    newValue.add(chain);
                }
                newValue.add("\n");
            }
        }
        if (splitChain == null) {
            return chain;
        } else {
            result = String.join("", newValue.toArray(new String[0]));
            return result;
        }
    }

    public List<String> getFunctionsNames() {
        return functionsNames;
    }

    public Queue<String> getErrorQueue() {
        return errorQueue;
    }

    public boolean isMain() {
        return mainFlag;
    }
    
}