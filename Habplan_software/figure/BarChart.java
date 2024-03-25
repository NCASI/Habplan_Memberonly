/**This is incomplete, but might have some ideas for a barchart

 /**
      * Draw a histogram of the letter frequency.
      * This method is triggered by repaint(), or by
      * window manager repaint events.
      */
    public void paint (Graphics g) {
        long maxCount = 0;
        for (int i=0; i<countArray.length; ++i) {
            if (countArray[i] > maxCount) maxCount = countArray[i];
        }

        Dimension d = getSize();
        double yScale = ((double)d.height) / ((double)maxCount);
        int barWidth = (int)d.width / countArray.length;
        int x = 0;
        for (int j=0; j<countArray.length; ++j) {
            g.setColor (Color.blue);
            int barHeight = (int)(countArray[j]*yScale);
            g.fillRect (x, d.height-barHeight,
                        barWidth, barHeight);
            g.setColor (Color.white);
            g.drawRect (x, d.height-barHeight,
                        barWidth, barHeight);
            g.setColor (Color.black);
            g.drawChars (letterArray, j, 1, x, 10);
            x += barWidth;
        }
    }
