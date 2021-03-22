package pl.coderslab.pojo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.coderslab.helper.NumberHelper;

import java.util.ArrayList;
import java.util.List;

@Component
public class DailyBalanceData {

    @Autowired
    private NumberHelper numberHelper;

    private BasicMacro receivedMacro;
    private BasicMacro neededMacro;
    private List<Object> dataList;
    private List<Double> helperList;
    private Counter counter;

    public DailyBalanceData(){
        this.receivedMacro = new ExtendMacro();
        this.neededMacro = new BasicMacro();
        this.dataList = new ArrayList<>();
        this.helperList = new ArrayList<>();
        this.counter = new Counter();
    }

    public void addData(double glycemicCharge){
        this.helperList.add(glycemicCharge);
        increment();
    }

    public void submit(){
        this.dataList.add(calculateResult());
        this.counter.number = 0;
        this.helperList.clear();
    }

    public void clearAll(){
        this.receivedMacro = new ExtendMacro();
        this.neededMacro = new BasicMacro();
        this.dataList.clear();
        this.helperList.clear();
        this.counter.setNumber(0);
    }

    private double calculateResult(){
        double sum = this.helperList.stream()
                .mapToDouble(Double::valueOf)
                .sum();
        if((sum == 0) || (counter.getNumber() == 0)){
            return  0.0;
        } else {
            return numberHelper.roundDouble((sum / counter.getNumber()));
        }
    }

    private void increment(){
        this.counter.increment();
    }

    public BasicMacro getReceivedMacro() {
        return receivedMacro;
    }

    public BasicMacro getNeededMacro() {
        return neededMacro;
    }

    public List<Object> getDataList() {
        return dataList;
    }

    private class Counter {
        private int number;

        private Counter(){

        }

        private void increment(){
            this.number = ++this.number;
        }

        public int getNumber() {
            return number;
        }

        public void setNumber(int number) {
            this.number = number;
        }
    }
}
