package com.fv.sdp.util;

import java.security.SecureRandom;

/**
 * Created by filip on 6/16/2017.
 */
public class RandomIdGenerator
{
    private static int seed;

    public static String getRndId()
    {
        SecureRandom random = new SecureRandom();

        return String.valueOf(random.nextInt(Integer.MAX_VALUE));
    }
}
