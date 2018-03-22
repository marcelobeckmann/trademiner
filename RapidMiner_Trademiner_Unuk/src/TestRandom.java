import java.text.NumberFormat;
import java.util.Random;

public class TestRandom {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumFractionDigits(2);
		
		Random random = new Random(System.currentTimeMillis());
		
		for (int i=1;i<10000;i++)
		{
			
			System.out.println(nf.format(random.nextGaussian()*100));
		}
		

	}

}
