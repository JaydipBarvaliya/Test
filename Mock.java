NativeWebRequest webRequest = mock(NativeWebRequest.class);
when(webRequest.getHeader("accept")).thenReturn("application/json");
when(esignatureEventsApiDelegateImpl.getRequest()).thenReturn(Optional.of(webRequest));