import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class Calculator extends JFrame {
	private enum NumberState{
		START,
		FRONT,
		BACK,
		ERROR
	};
	private NumberState numberState; 
	
	private enum StatementState{
		START,
		OP,
	}
	private StatementState statementState;
	private final StringBuilder statementBuilder;
	private final ScriptEngine statementExecutor;
	private final ScriptEngineManager sem;
	
	private Number evaluateCurrentStatement() throws ScriptException{
		return (Number)statementExecutor.eval(statementBuilder.toString());		
	}
	
	private NumberState getStateForNumber(Number n){
		String ns=n.toString();
		if(ns.contains(".")){
			return NumberState.BACK;
		}else{
			return NumberState.FRONT;
		}
	}
	
	private void initMenu(){
		JMenuBar bar=new JMenuBar();
		
		JMenuItem helpItem=new JMenuItem();
		helpItem.setName("mi_Help");
		helpItem.setText("Help");
		bar.add(helpItem);
		
		JMenuItem exitItem=new JMenuItem();
		exitItem.setName("mi_Exit");
		exitItem.setText("Exit");
		bar.add(exitItem);
		
		this.setJMenuBar(bar);
	}
	
	private void initDisplay(){
		JPanel displayPanel=new JPanel();
		displayPanel.setLayout(new BoxLayout(displayPanel,BoxLayout.Y_AXIS));
		displayPanel.setName("jp_Display");
				 		
		JTextField currentCalcField=new JTextField();
		currentCalcField.setName("tf_CurCalc");
		currentCalcField.setHorizontalAlignment(JTextField.RIGHT);
		currentCalcField.setEditable(false);
		displayPanel.add(currentCalcField);
		
		JTextField currentNumberField=new JTextField();
		currentNumberField.setName("tf_CurNumber");
		currentNumberField.setHorizontalAlignment(JTextField.RIGHT);
		currentNumberField.setEditable(false);
		displayPanel.add(currentNumberField);
		
		this.add(displayPanel,BorderLayout.NORTH);
	}
	
	private JButton createButton(String name,String text,ActionListener listener){
		JButton btn=new JButton();
		btn.setName(name);
		btn.setText(text);
		btn.addActionListener(listener);
		return btn;
	}
	
	private class ValueButtonListener implements ActionListener{
		
		private String valueString;
		public ValueButtonListener(int value){
			valueString=String.valueOf(value);
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			switch(numberState){
			case START:
				currentNumberField.setText(valueString);
				numberState=NumberState.FRONT;
				break;
			case FRONT:
				currentNumberField.setText(currentNumberField.getText()+valueString);
				break;
			case BACK:
				currentNumberField.setText(currentNumberField.getText()+valueString);
				break;
			case ERROR:
				break;
			}
			
		}
	}
	

	
	private void initButtons(){
		JPanel buttonPanel=new JPanel();
		buttonPanel.setLayout(new GridLayout(4, 4));
		buttonPanel.setName("jp_Buttons");
		
		buttonPanel.add(createButton("btn_Seven","7",new ValueButtonListener(7)));
		buttonPanel.add(createButton("btn_Eight","8",new ValueButtonListener(8)));
		buttonPanel.add(createButton("btn_Nine","9",new ValueButtonListener(9)));
		buttonPanel.add(createButton("btn_Add","+",null));
		
		buttonPanel.add(createButton("btn_Four","4",new ValueButtonListener(4)));
		buttonPanel.add(createButton("btn_Five","5",new ValueButtonListener(5)));
		buttonPanel.add(createButton("btn_Six","6",new ValueButtonListener(6)));
		buttonPanel.add(createButton("btn_Sub","-",null));
		
		buttonPanel.add(createButton("btn_One","1",new ValueButtonListener(1)));
		buttonPanel.add(createButton("btn_Two","2",new ValueButtonListener(2)));
		buttonPanel.add(createButton("btn_Three","3",new ValueButtonListener(3)));
		buttonPanel.add(createButton("btn_Div","/",new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				if(numberState==NumberState.START){
					numberState=NumberState.ERROR;
					currentCalcField.setText("");
					currentNumberField.setText("ERROR: No Number");	
				}else if(numberState==NumberState.FRONT||numberState==NumberState.BACK){
					statementBuilder.append(currentNumberField.getText()+"/");
					currentCalcField.setText(statementBuilder.toString());
					statementState=StatementState.OP;
					currentNumberField.setText("");
					numberState=NumberState.START;
				}
				
			}
			
		}));
		
		buttonPanel.add(createButton("btn_Zero","0",new ValueButtonListener(0)));
		buttonPanel.add(createButton("btn_Dot",".",new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				switch(numberState){
				case START:
					currentNumberField.setText("0.");
					numberState=NumberState.BACK;
					break;
				case FRONT:
					currentNumberField.setText(currentNumberField.getText()+".");
					numberState=NumberState.BACK;
					break;
				case BACK:
					currentNumberField.setText("Input Error");
					numberState=NumberState.ERROR;
					break;
				case ERROR:
					break;
				}
				
			}
		}));
		
		
		buttonPanel.add(createButton("btn_Clear","Clear",new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				numberState=NumberState.START;
				currentNumberField.setText("");
				currentCalcField.setText("");
				statementBuilder.setLength(0);
				statementState=StatementState.START;
			}
			
		}));
		buttonPanel.add(createButton("btn_Equals","=",new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(statementState==StatementState.START){
					if(numberState==NumberState.START){
						currentCalcField.setText("");
						currentNumberField.setText("ERROR: No Number");
						numberState=NumberState.ERROR;
					}else{
						currentCalcField.setText("");
						statementState=StatementState.START;
					}
				}else{
					if(numberState==NumberState.START){
						currentCalcField.setText("");
						currentNumberField.setText("ERROR: No Number");
						numberState=NumberState.ERROR;
					}else if(numberState==NumberState.FRONT||numberState==NumberState.BACK){
						try{
							statementBuilder.append(currentNumberField.getText());
							Number n=evaluateCurrentStatement();
							currentCalcField.setText("");
							statementState=StatementState.START;
							currentNumberField.setText(n.toString());
							numberState=getStateForNumber(n);
							
						}catch(ScriptException ex){
							currentNumberField.setText("ERROR: Script Failed");
							numberState=NumberState.ERROR;
						}
					}
				}
			}
		}));
		
		this.add(buttonPanel);
	}
	
	//Finds a component by its name. Kinda strange that it's not included.
	//Uses Breadth First Search. Because why not.
	private Component findByName(String name){
		Set<Component> seen=new HashSet<>();
		Queue<Component> searchQueue=new LinkedList<>();
		searchQueue.add(this);
		
		while(!searchQueue.isEmpty()){
			 Component cur=searchQueue.remove();
			 if(seen.contains(cur)){
				continue; 
			 }
			 seen.add(cur);
			 if(!(cur.getName()==name)){
				 if(cur instanceof Container){
					 Container curContainer=(Container)cur;
					 for(Component child:curContainer.getComponents()){
						 searchQueue.add(child);
					 }
				 }
			 }else{
				 return cur;
			 }
		}
		return null;
	}
	
	private final JTextField currentNumberField;
	private final JTextField currentCalcField;
	public Calculator(){
		//Initialize Script Evaluation Stuff
		sem=new ScriptEngineManager();
		statementExecutor=sem.getEngineByName("JavaScript");
		
		statementBuilder=new StringBuilder();
		
		//Initialize the display stuff.
		JPanel root=new JPanel();
		root.setLayout(new BorderLayout());
		
		initMenu();
		initDisplay();
		initButtons();
		
		currentNumberField=(JTextField)findByName("tf_CurNumber");
		currentCalcField=(JTextField)findByName("tf_CurCalc");
		pack();
		statementState=StatementState.START;
		numberState=NumberState.START;
		setResizable(false);
	}
	
	public void run(){
		this.setVisible(true);
	}
	
	public static void main(String[] args) {
		Calculator instance=new Calculator();
		instance.run();
		// TODO Auto-generated method stub

	}

}
