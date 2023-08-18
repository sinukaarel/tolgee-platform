import { ComponentProps, FunctionComponent } from 'react';
import { Box, Button } from '@mui/material';
import { SpinnerProgress } from 'tg.component/SpinnerProgress';

const LoadingButton: FunctionComponent<
  ComponentProps<typeof Button> & { loading?: boolean }
> = (props) => {
  const { disabled, children, loading, ...otherProps } = props;

  const isDisabled = loading || disabled;

  return (
    <Button disabled={isDisabled} {...otherProps}>
      {loading && (
        <Box
          display="flex"
          position="absolute"
          top="0"
          right="0"
          bottom="0"
          left="0"
          alignItems="center"
          justifyContent="center"
          data-cy="global-loading"
        >
          <SpinnerProgress size={20} />
        </Box>
      )}
      {children}
    </Button>
  );
};

export default LoadingButton;
