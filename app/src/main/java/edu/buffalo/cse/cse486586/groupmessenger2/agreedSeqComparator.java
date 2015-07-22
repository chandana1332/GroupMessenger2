package edu.buffalo.cse.cse486586.groupmessenger2;

import java.util.Comparator;

/**
 * Created by chandana on 3/13/15.
 */
public class agreedSeqComparator implements Comparator<QueueObject> {





        @Override
        public int compare(QueueObject x, QueueObject y)
        {
            // Assume neither string is null. Real code should
            // probably be more robust
            // You could also just return x.length() - y.length(),
            // which would be more efficient.
            if (((y.agreedSeq - x.agreedSeq))>0.0)
            {
                return 1;
            }
           else if (((y.agreedSeq - x.agreedSeq))<0.0)
            {
                return -1;
            }
            else if(x.agreedSeq==y.agreedSeq)
            {
                return (Integer.parseInt(y.messagePort)-Integer.parseInt(x.messagePort));
            }
            return 0;
        }



}
